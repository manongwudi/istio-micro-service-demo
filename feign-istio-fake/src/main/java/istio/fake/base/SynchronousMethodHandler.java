/**
 * Copyright 2012-2019 The Feign Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package istio.fake.base;

import static istio.fake.util.Util.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import istio.fake.FakeException;
import istio.fake.util.Util;
import istio.fake.base.codec.Decoder;
import istio.fake.base.codec.ErrorDecoder;
import istio.fake.base.log.FakeLogger;
import istio.fake.openfake.Target;
import istio.fake.support.HttpRequestHeaderHolder;

public final class SynchronousMethodHandler implements InvocationHandlerFactory.MethodHandler {

    private static final long MAX_RESPONSE_BUFFER_SIZE = 8192L;

    private final MethodMetadata metadata;
    private final Target<?> target;
    private final Client client;
    private final List<RequestInterceptor> requestInterceptors;
    private final FakeLogger logger;
    private final FakeLogger.Level logLevel;
    private final RequestTemplate.Factory buildTemplateFromArgs;
    private final Request.Options options;
    private final Decoder decoder;
    private final ErrorDecoder errorDecoder;
    private final boolean decode404;
    private final boolean closeAfterDecode;
    private final ExceptionPropagationPolicy propagationPolicy;
    private final HttpRequestHeaderHolder httpRequestHeaderHolder;

    private SynchronousMethodHandler(Target<?> target, Client client,
            List<RequestInterceptor> requestInterceptors, FakeLogger logger,
            FakeLogger.Level logLevel, MethodMetadata metadata,
            RequestTemplate.Factory buildTemplateFromArgs, Request.Options options,
            Decoder decoder, ErrorDecoder errorDecoder, boolean decode404,
            boolean closeAfterDecode, ExceptionPropagationPolicy propagationPolicy,
            HttpRequestHeaderHolder httpRequestHeaderHolder) {
        this.target = checkNotNull(target, "target");
        this.client = checkNotNull(client, "client for %s", target);
        this.requestInterceptors =
                checkNotNull(requestInterceptors, "requestInterceptors for %s", target);
        this.logger = checkNotNull(logger, "logger for %s", target);
        this.logLevel = checkNotNull(logLevel, "logLevel for %s", target);
        this.metadata = checkNotNull(metadata, "metadata for %s", target);
        this.buildTemplateFromArgs = checkNotNull(buildTemplateFromArgs, "metadata for %s", target);
        this.options = checkNotNull(options, "options for %s", target);
        this.errorDecoder = checkNotNull(errorDecoder, "errorDecoder for %s", target);
        this.decoder = checkNotNull(decoder, "decoder for %s", target);
        this.decode404 = decode404;
        this.closeAfterDecode = closeAfterDecode;
        this.propagationPolicy = propagationPolicy;
        this.httpRequestHeaderHolder = httpRequestHeaderHolder;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        RequestTemplate template = buildTemplateFromArgs.create(argv);
        try {
            return executeAndDecode(template);
        } catch (Exception e) {
            if (logLevel != FakeLogger.Level.NONE) {
                logger.logRetry(metadata.configKey(), logLevel);
            }
            throw e;
        }
    }

    Object executeAndDecode(RequestTemplate template) throws Throwable {
        Request request = targetRequest(template);

        if (logLevel != FakeLogger.Level.NONE) {
            logger.logRequest(metadata.configKey(), logLevel, request);
        }

        Response response;
        long start = System.nanoTime();
        try {
            response = client.execute(request, options);
        } catch (IOException e) {
            if (logLevel != FakeLogger.Level.NONE) {
                logger.logIOException(metadata.configKey(), logLevel, e, elapsedTime(start));
            }
            throw FakeException.errorExecuting(request, e);
        }
        long elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        boolean shouldClose = true;
        try {
            if (logLevel != FakeLogger.Level.NONE) {
                response =
                        logger.logAndRebufferResponse(metadata.configKey(), logLevel, response, elapsedTime);
            }
            if (Response.class == metadata.returnType()) {
                if (response.body() == null) {
                    return response;
                }
                if (response.body().length() == null ||
                        response.body().length() > MAX_RESPONSE_BUFFER_SIZE) {
                    shouldClose = false;
                    return response;
                }
                // Ensure the response body is disconnected
                byte[] bodyData = Util.toByteArray(response.body().asInputStream());
                return response.toBuilder().body(bodyData).build();
            }
            if (response.status() >= 200 && response.status() < 300) {
                if (void.class == metadata.returnType()) {
                    return null;
                } else {
                    Object result = decode(response);
                    shouldClose = closeAfterDecode;
                    return result;
                }
            } else if (decode404 && response.status() == 404 && void.class != metadata.returnType()) {
                Object result = decode(response);
                shouldClose = closeAfterDecode;
                return result;
            } else {
                throw errorDecoder.decode(metadata.configKey(), response);
            }
        } catch (IOException e) {
            if (logLevel != FakeLogger.Level.NONE) {
                logger.logIOException(metadata.configKey(), logLevel, e, elapsedTime);
            }
            throw FakeException.errorReading(request, response, e);
        } finally {
            if (shouldClose) {
                Util.ensureClosed(response.body());
            }
        }
    }

    long elapsedTime(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    Request targetRequest(RequestTemplate template) {
        for (RequestInterceptor interceptor : requestInterceptors) {
            interceptor.apply(template);
        }
        return target.apply(template);
    }

    Object decode(Response response) throws Throwable {
        try {
            return decoder.decode(response, metadata.returnType());
        } catch (FakeException e) {
            throw e;
        }
    }

    public static class Factory {

        private final Client client;
        private final List<RequestInterceptor> requestInterceptors;
        private final FakeLogger logger;
        private final FakeLogger.Level logLevel;
        private final boolean decode404;
        private final boolean closeAfterDecode;
        private final ExceptionPropagationPolicy propagationPolicy;
        private final HttpRequestHeaderHolder httpRequestHeaderHolder;

        public Factory(Client client, List<RequestInterceptor> requestInterceptors,
                FakeLogger logger, FakeLogger.Level logLevel, boolean decode404, boolean closeAfterDecode,
                ExceptionPropagationPolicy propagationPolicy, HttpRequestHeaderHolder httpRequestHeaderHolder) {
            this.client = checkNotNull(client, "client");
            this.requestInterceptors = checkNotNull(requestInterceptors, "requestInterceptors");
            this.logger = checkNotNull(logger, "logger");
            this.logLevel = checkNotNull(logLevel, "logLevel");
            this.decode404 = decode404;
            this.closeAfterDecode = closeAfterDecode;
            this.propagationPolicy = propagationPolicy;
            this.httpRequestHeaderHolder = httpRequestHeaderHolder;
        }

        public InvocationHandlerFactory.MethodHandler create(Target<?> target,
                MethodMetadata md,
                RequestTemplate.Factory buildTemplateFromArgs,
                Request.Options options,
                Decoder decoder,
                ErrorDecoder errorDecoder) {
            return new SynchronousMethodHandler(target, client, requestInterceptors, logger,
                    logLevel, md, buildTemplateFromArgs, options, decoder,
                    errorDecoder, decode404, closeAfterDecode, propagationPolicy, httpRequestHeaderHolder);
        }
    }
}
