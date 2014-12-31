package serializers.impl.thrift;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import serializers.spi.ObjectSerializer;
import domain.thrift.Image;
import domain.thrift.Media;
import domain.thrift.MediaContent;
import domain.thrift.Person;
import domain.thrift.Player;
import domain.thrift.Size;

public class ThriftSerializer implements ObjectSerializer<MediaContent> {
	public int expectedSize = 0;
	public final static int ITERATIONS = 100000;

	@Override
	public MediaContent create() {
		Media media = new Media();
		media.setUri("http://javaone.com/keynote.mpg");
		media.setFormat("video/mpg4");
		media.setTitle("Javaone Keynote");
		media.setDuration(1234567);
		media.setBitrate(0);
		media.setSize(123);
		media.setWidth(0);
		media.setHeight(0);
		media.addToPerson(new Person("Bill Gates"));
		media.addToPerson(new Person("Steve Jobs"));
		media.setPlayer(Player.JAVA);

		// Image image1 = new Image("http://javaone.com/keynote_large.jpg", "Javaone Keynote", 0, 0,
		// Size.LARGE);
		Image image1 = new Image("http://javaone.com/keynote_large.jpg");
		image1.setTitle("Javaone Keynote");
		image1.setSize(Size.LARGE);
		Image image2 = new Image("http://javaone.com/keynote_thumbnail.jpg");
		image2.setTitle("Javaone Keynote");
		image2.setSize(Size.SMALL);

		MediaContent content = new MediaContent();
		content.setMedia(media);
		content.addToImage(image1);
		content.addToImage(image2);
		return content;
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		TIOStreamTransport trans = new TIOStreamTransport(bais);
		TBinaryProtocol oprot = new TBinaryProtocol(trans);
		MediaContent content = new MediaContent();
		content.read(oprot);
		return content;
	}

	@Override
	public String getName() {
		return "thrift";
	}

	@Override
	public byte[] serialize(MediaContent content) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedSize);
		TIOStreamTransport trans = new TIOStreamTransport(baos);
		TBinaryProtocol oprot = new TBinaryProtocol(trans);
		content.write(oprot);
		byte[] array = baos.toByteArray();
		expectedSize = array.length;
		return array;
	}

}
