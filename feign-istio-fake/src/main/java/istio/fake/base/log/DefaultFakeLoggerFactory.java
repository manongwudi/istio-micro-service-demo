package istio.fake.base.log;

public class DefaultFakeLoggerFactory implements FakeLoggerFactory {

	private FakeLogger fakeLogger;

	public DefaultFakeLoggerFactory(FakeLogger fakeLogger) {
		this.fakeLogger = fakeLogger;
	}

	@Override
	public FakeLogger create(Class<?> type) {
		return this.fakeLogger != null ? this.fakeLogger : new Slf4JFakeLogger(type);
	}

}