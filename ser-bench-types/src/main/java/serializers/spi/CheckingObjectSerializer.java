package serializers.spi;

import serializers.TextSource;

public abstract class CheckingObjectSerializer<T> implements ObjectSerializer<T> {

	protected TextSource ts = TextSource.ASCII;

	public final void assetEquals(int expected, int actual) {
		if (expected != actual) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	public final void assetEquals(long expected, long actual) {
		if (expected != actual) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	public final void assetEquals(Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new RuntimeException("" + expected + "!=" + actual);
		}
	}

	public abstract void checkAllFields(T obj);

	public abstract void checkMediaField(T obj);

	public TextSource getTs() {
		return ts;
	}

	public void setTs(TextSource ts) {
		this.ts = ts;
	}

}