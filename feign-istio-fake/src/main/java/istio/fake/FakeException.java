/**
 * Copyright 2012-2019 The Feign Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package istio.fake;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import istio.fake.util.Util;
import istio.fake.base.Request;
import istio.fake.base.Response;

/**
 * Similar to {@code javax.websocket.EncodeException}, raised when a problem occurs encoding a
 * message. Note that {@code EncodeException} is not an {@code IOException}, nor does it have one
 * set as its cause.
 */
public class FakeException extends ResponseStatusException {

    private static final long serialVersionUID = 1L;

    private int statusCode;
    private byte[] content;
    private String reason;

    public int getStatusCode() {
        return statusCode;
    }


    public byte[] getContent() {
        return content;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    /**
     * @param message the reason for the failure.
     */
    public FakeException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * @param message possibly null reason for the failure.
     * @param cause   the cause of the error.
     */
    public FakeException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }


    public FakeException(int statusCode, String message) {
        super(HttpStatus.valueOf(statusCode), message);
        this.statusCode = statusCode;
    }

    protected FakeException(int statusCode, String message, byte[] content) {
        super(HttpStatus.valueOf(statusCode));
        String contentMsg = content2msg(content);
        this.reason = StringUtils.hasText(contentMsg) ? contentMsg : message;
        this.statusCode = statusCode;
        this.content = content;
    }

    private String content2msg(byte[] content) {
        try {
            JSONObject jsonObj = JSON.parseObject(new String(content, StandardCharsets.UTF_8));
            return jsonObj.getString("msg");
        } catch (Exception e) {
            return null;
        }
    }

    protected FakeException(int statusCode, String message, Throwable cause, byte[] content) {
        super(HttpStatus.valueOf(statusCode), message, cause);
        String contentMsg = content2msg(content);
        this.reason = StringUtils.hasText(contentMsg) ? contentMsg : message;
        this.statusCode = statusCode;
        this.content = content;
    }

    protected FakeException(int statusCode, String message, Throwable cause) {
        super(HttpStatus.valueOf(statusCode), message, cause);
        this.statusCode = statusCode;
    }

    private static FakeException errorStatus(int status, String message, byte[] body) {
        switch (status) {
            case 400:
                return new BadRequest(message, body);
            case 401:
                return new Unauthorized(message, body);
            case 403:
                return new Forbidden(message, body);
            case 404:
                return new NotFound(message, body);
            case 405:
                return new MethodNotAllowed(message, body);
            case 406:
                return new NotAcceptable(message, body);
            case 409:
                return new Conflict(message, body);
            case 410:
                return new Gone(message, body);
            case 415:
                return new UnsupportedMediaType(message, body);
            case 429:
                return new TooManyRequests(message, body);
            case 422:
                return new UnprocessableEntity(message, body);
            case 500:
                return new InternalServerError(message, body);
            case 501:
                return new NotImplemented(message, body);
            case 502:
                return new BadGateway(message, body);
            case 503:
                return new ServiceUnavailable(message, body);
            case 504:
                return new GatewayTimeout(message, body);
            default:
                return new FakeException(status, message, body);
        }
    }

    public static class BadRequest extends FakeException {
        public BadRequest(String message, byte[] body) {
            super(400, message, body);
        }
    }

    public static class Unauthorized extends FakeException {
        public Unauthorized(String message, byte[] body) {
            super(401, message, body);
        }
    }

    public static class Forbidden extends FakeException {
        public Forbidden(String message, byte[] body) {
            super(403, message, body);
        }
    }

    public static class NotFound extends FakeException {
        public NotFound(String message, byte[] body) {
            super(404, message, body);
        }
    }

    public static class MethodNotAllowed extends FakeException {
        public MethodNotAllowed(String message, byte[] body) {
            super(405, message, body);
        }
    }

    public static class NotAcceptable extends FakeException {
        public NotAcceptable(String message, byte[] body) {
            super(406, message, body);
        }
    }

    public static class Conflict extends FakeException {
        public Conflict(String message, byte[] body) {
            super(409, message, body);
        }
    }

    public static class Gone extends FakeException {
        public Gone(String message, byte[] body) {
            super(410, message, body);
        }
    }

    public static class UnsupportedMediaType extends FakeException {
        public UnsupportedMediaType(String message, byte[] body) {
            super(415, message, body);
        }
    }

    public static class TooManyRequests extends FakeException {
        public TooManyRequests(String message, byte[] body) {
            super(429, message, body);
        }
    }

    public static class UnprocessableEntity extends FakeException {
        public UnprocessableEntity(String message, byte[] body) {
            super(422, message, body);
        }
    }

    public static class InternalServerError extends FakeException {
        public InternalServerError(String message, byte[] body) {
            super(500, message, body);
        }
    }

    public static class NotImplemented extends FakeException {
        public NotImplemented(String message, byte[] body) {
            super(501, message, body);
        }
    }

    public static class BadGateway extends FakeException {
        public BadGateway(String message, byte[] body) {
            super(502, message, body);
        }
    }

    public static class ServiceUnavailable extends FakeException {
        public ServiceUnavailable(String message, byte[] body) {
            super(503, message, body);
        }
    }

    public static class GatewayTimeout extends FakeException {
        public GatewayTimeout(String message, byte[] body) {
            super(504, message, body);
        }
    }

    public static FakeException errorStatus(String methodKey, Response response) {
        String message = format("status %s reading %s", response.status(), methodKey);

        byte[] body = {};
        try {
            if (response.body() != null) {
                body = Util.toByteArray(response.body().asInputStream());
            }
        } catch (IOException ignored) { // NOPMD
        }

        return errorStatus(response.status(), message, body);
    }


    public static FakeException errorExecuting(Request request, IOException cause) {
        return new FakeException(
                -1,
                format("%s executing %s %s", cause.getMessage(), request.httpMethod(), request.url()),
                cause);
    }

    public static FakeException errorReading(Request request, Response response, IOException cause) {
        return new FakeException(
                response.status(),
                format("%s reading %s %s", cause.getMessage(), request.httpMethod(), request.url()),
                cause,
                request.requestBody().asBytes());
    }

    @Override
    public String toString() {
        return "FakeException{" +
                "status=" + statusCode +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
