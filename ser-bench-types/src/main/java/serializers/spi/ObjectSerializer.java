package serializers.spi;

public interface ObjectSerializer<T> {
	public T create() throws Exception;

	public T deserialize(byte[] array) throws Exception;

	public String getName();

	public byte[] serialize(T content) throws Exception;
}
