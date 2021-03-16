/*
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package istio.fake.openfake;

import static istio.fake.base.ExceptionPropagationPolicy.NONE;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import istio.fake.base.Client;
import istio.fake.base.Contract;
import istio.fake.base.ExceptionPropagationPolicy;
import istio.fake.base.InvocationHandlerFactory;
import istio.fake.base.QueryMapEncoder;
import istio.fake.base.ReflectiveFake;
import istio.fake.base.Request;
import istio.fake.base.RequestInterceptor;
import istio.fake.base.Response;
import istio.fake.base.ResponseMapper;
import istio.fake.base.SynchronousMethodHandler;
import istio.fake.base.codec.Decoder;
import istio.fake.base.codec.Encoder;
import istio.fake.base.codec.ErrorDecoder;
import istio.fake.base.log.FakeLogger;
import istio.fake.support.HttpRequestHeaderHolder;

/**
 * Feign's purpose is to ease development against http apis that feign restfulness. <br> In
 * implementation, Feign is a {@link Fake#newInstance factory} for generating {@link Target
 * targeted} http apis.
 */
public abstract class Fake {

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Configuration keys are formatted as unresolved <a href= "http://docs.oracle.com/javase/6/docs/jdk/api/javadoc/doclet/com/sun/javadoc/SeeTag.html"
     * >see tags</a>. This method exposes that format, in case you need to create the same value as
     * {@link MethodMetadata#configKey()} for correlation purposes.
     *
     * <p>Here are some sample encodings:
     *
     * <pre>
     * <ul>
     *   <li>{@code Route53}: would match a class {@code route53.Route53}</li>
     *   <li>{@code Route53#list()}: would match a method {@code route53.Route53#list()}</li>
     *   <li>{@code Route53#listAt(Marker)}: would match a method {@code
     * route53.Route53#listAt(Marker)}</li>
     *   <li>{@code Route53#listByNameAndType(String, String)}: would match a method {@code
     * route53.Route53#listAt(String, String)}</li>
     * </ul>
     * </pre>
     * <p>
     * Note that there is no whitespace expected in a key!
     *
     * @param targetType {@link feign.Target#type() type} of the Feign interface.
     * @param method     invoked method, present on {@code type} or its super.
     * @see MethodMetadata#configKey()
     */
    public static String configKey(Class targetType, Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetType.getSimpleName());
        builder.append('#').append(method.getName()).append('(');
        for (Type param : method.getGenericParameterTypes()) {
            param = Types.resolve(targetType, targetType, param);
            builder.append(Types.getRawType(param).getSimpleName()).append(',');
        }
        if (method.getParameterTypes().length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.append(')').toString();
    }

    /**
     * @deprecated use {@link #configKey(Class, Method)} instead.
     */
    @Deprecated
    public static String configKey(Method method) {
        return configKey(method.getDeclaringClass(), method);
    }

    /**
     * Returns a new instance of an HTTP API, defined by annotations in the {@link Fake Contract},
     * for the specified {@code target}. You should cache this result.
     */
    public abstract <T> T newInstance(Target<T> target);

    public static class Builder {

        private final List<RequestInterceptor> requestInterceptors =
                new ArrayList<RequestInterceptor>();
        private FakeLogger.Level logLevel = FakeLogger.Level.NONE;
        private Contract contract = new Contract.Default();
        private Client client = new Client.Default(null, null);
        private FakeLogger logger = new FakeLogger.NoOpFakeLogger();
        private Encoder encoder = new Encoder.Default();
        private Decoder decoder = new Decoder.Default();
        private QueryMapEncoder queryMapEncoder = new QueryMapEncoder.Default();
        private ErrorDecoder errorDecoder = new ErrorDecoder.Default();
        private Request.Options options = new Request.Options();
        private InvocationHandlerFactory invocationHandlerFactory =
                new InvocationHandlerFactory.Default();
        private boolean decode404;
        private boolean closeAfterDecode = true;
        private ExceptionPropagationPolicy propagationPolicy = NONE;
        private HttpRequestHeaderHolder httpRequestHeaderHolder = new HttpRequestHeaderHolder() {
            @Override
            public Map<String, Object> getHeaderMap() {
                return null;
            }
        };

        public Builder headerHolder(HttpRequestHeaderHolder httpRequestHeaderHolder) {
            this.httpRequestHeaderHolder = httpRequestHeaderHolder;
            return this;
        }

        public Builder logLevel(FakeLogger.Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder contract(Contract contract) {
            this.contract = contract;
            return this;
        }

        public Builder client(Client client) {
            this.client = client;
            return this;
        }

        public Builder logger(FakeLogger logger) {
            this.logger = logger;
            return this;
        }

        public Builder encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder decoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        /**
         * Allows to map the response before passing it to the decoder.
         */
        public Builder mapAndDecode(ResponseMapper mapper, Decoder decoder) {
            this.decoder = new ResponseMappingDecoder(mapper, decoder);
            return this;
        }

        /**
         * This flag indicates that the {@link #decoder(Decoder) decoder} should process responses with
         * 404 status, specifically returning null or empty instead of throwing {@link FeignException}.
         * <p>
         * <p/> All first-party (ex gson) decoders return well-known empty values defined by {@link
         * Util#emptyValueOf}. To customize further, wrap an existing {@link #decoder(Decoder) decoder}
         * or make your own.
         * <p>
         * <p/> This flag only works with 404, as opposed to all or arbitrary status codes. This was an
         * explicit decision: 404 -> empty is safe, common and doesn't complicate redirection, retry or
         * fallback policy. If your server returns a different status for not-found, correct via a
         * custom {@link #client(Client) client}.
         *
         * @since 8.12
         */
        public Builder decode404() {
            this.decode404 = true;
            return this;
        }

        public Builder errorDecoder(ErrorDecoder errorDecoder) {
            this.errorDecoder = errorDecoder;
            return this;
        }

        public Builder options(Request.Options options) {
            this.options = options;
            return this;
        }

        /**
         * Adds a single request interceptor to the builder.
         */
        public Builder requestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptors.add(requestInterceptor);
            return this;
        }

        /**
         * Sets the full set of request interceptors for the builder, overwriting any previous
         * interceptors.
         */
        public Builder requestInterceptors(Iterable<RequestInterceptor> requestInterceptors) {
            this.requestInterceptors.clear();
            for (RequestInterceptor requestInterceptor : requestInterceptors) {
                this.requestInterceptors.add(requestInterceptor);
            }
            return this;
        }

        /**
         * Allows you to override how reflective dispatch works inside of Feign.
         */
        public Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            this.invocationHandlerFactory = invocationHandlerFactory;
            return this;
        }

        public <T> T target(Class<T> apiType, String url) {
            return target(new Target.HardCodedTarget<T>(apiType, url));
        }

        public <T> T target(Target<T> target) {
            return build().newInstance(target);
        }

        public Fake build() {
            SynchronousMethodHandler.Factory synchronousMethodHandlerFactory =
                    new SynchronousMethodHandler.Factory(client, requestInterceptors, logger,
                            logLevel, decode404, closeAfterDecode, propagationPolicy, httpRequestHeaderHolder);
            ReflectiveFake.ParseHandlersByName handlersByName =
                    new ReflectiveFake.ParseHandlersByName(contract, options, encoder, decoder, queryMapEncoder,
                            errorDecoder, synchronousMethodHandlerFactory, httpRequestHeaderHolder);
            return new ReflectiveFake(handlersByName, invocationHandlerFactory, queryMapEncoder);
        }
    }

    static class ResponseMappingDecoder implements Decoder {

        private final ResponseMapper mapper;
        private final Decoder delegate;

        ResponseMappingDecoder(ResponseMapper mapper, Decoder decoder) {
            this.mapper = mapper;
            this.delegate = decoder;
        }

        @Override
        public Object decode(Response response, Type type) throws IOException {
            return delegate.decode(mapper.map(response, type), type);
        }
    }
}
