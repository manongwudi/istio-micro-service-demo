package istio.fake.base.log;

public interface FakeLoggerFactory {

	/**
	 * Factory method to provide a {@link FakeLogger} for a given {@link Class}.
	 * @param type the {@link Class} for which a {@link FakeLogger} instance is to be created
	 * @return a {@link FakeLogger} instance
	 */
	FakeLogger create(Class<?> type);

}