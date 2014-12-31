package serializers.spi;

public interface CheckingObjectSerializer<T> extends ObjectSerializer<T> {
	default void assetEquals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	default void assetEquals(long expected, long actual) {
		if (expected != actual) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	default void assetEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	public void checkAllFields(T obj);

	public void checkMediaField(T obj);
}