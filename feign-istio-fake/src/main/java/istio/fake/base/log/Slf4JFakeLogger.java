package istio.fake.base.log;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import istio.fake.base.Request;
import istio.fake.base.Response;

public class Slf4JFakeLogger extends FakeLogger {

  private final Logger logger;

  public Slf4JFakeLogger() {
    this(FakeLogger.class);
  }

  public Slf4JFakeLogger(Class<?> clazz) {
    this(LoggerFactory.getLogger(clazz));
  }

  public Slf4JFakeLogger(String name) {
    this(LoggerFactory.getLogger(name));
  }

  Slf4JFakeLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void logRequest(String configKey, Level logLevel, Request request) {
    if (logger.isDebugEnabled()) {
      super.logRequest(configKey, logLevel, request);
    }
  }

  @Override
  public Response logAndRebufferResponse(String configKey,
                                            Level logLevel,
                                            Response response,
                                            long elapsedTime)
      throws IOException {
    if (logger.isDebugEnabled()) {
      return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
    }
    return response;
  }

  @Override
  protected void log(String configKey, String format, Object... args) {
    // Not using SLF4J's support for parameterized messages (even though it would be more efficient)
    // because it would
    // require the incoming message formats to be SLF4J-specific.
    if (logger.isDebugEnabled()) {
      logger.debug(String.format(methodTag(configKey) + format, args));
    }
  }
}
