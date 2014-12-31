package serializers.impl.jdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import serializers.impl.StdMediaSerializer;
import domain.std.MediaContent;

public class JavaSerializer extends StdMediaSerializer {
	public int expectedSize = 0;

	public JavaSerializer() {
		this("java");
	}

	public JavaSerializer(String name) {
		super(name);
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array));
		return (MediaContent) ois.readObject();
	}

	@Override
	public byte[] serialize(MediaContent content) throws IOException, Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedSize);
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(content);
		byte[] array = baos.toByteArray();
		expectedSize = array.length;
		return array;
	}

}
