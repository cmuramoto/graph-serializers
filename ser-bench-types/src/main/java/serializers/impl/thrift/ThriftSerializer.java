package serializers.impl.thrift;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import serializers.spi.CheckingObjectSerializer;
import domain.thrift.Image;
import domain.thrift.Media;
import domain.thrift.MediaContent;
import domain.thrift.Person;
import domain.thrift.Player;
import domain.thrift.Size;

public class ThriftSerializer extends CheckingObjectSerializer<MediaContent> {
	public int expectedSize = 0;
	public final static int ITERATIONS = 100000;

	@Override
	public void checkAllFields(MediaContent content) {
		checkMediaField(content);

		Iterator<Image> list = content.getImageIterator();
		// assetEquals(2, list.size());

		Image image = list.next();
		assetEquals(image.getUri(), ts.largeImageUrl());
		assetEquals(image.getSize(), Size.LARGE);
		assetEquals(image.getTitle(), ts.tag());
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);

		image = list.next();
		assetEquals(image.getUri(), ts.smallImageUrl());
		assetEquals(image.getSize(), Size.SMALL);
		assetEquals(image.getTitle(), ts.tag());
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);

	}

	@Override
	public void checkMediaField(MediaContent obj) {
		Media media = obj.getMedia();

		assetEquals(media.getUri(), ts.mediaUrl());
		assetEquals(media.getFormat(), "video/mpg4");
		assetEquals(media.getTitle(), ts.tag());
		assetEquals(media.getDuration(), 1234567L);
		assetEquals(media.getSize(), 123L);
		assetEquals(media.getBitrate(), 0);
		assetEquals(media.getPlayer(), Player.JAVA);
		assetEquals(media.getWidth(), 0);
		assetEquals(media.getHeight(), 0);

		Iterator<Person> list = media.getPersonIterator();
		// assetEquals(2, list.size());
		int c = 0;
		for (; list.hasNext();) {
			String name = list.next().getName();
			if (name.equals(ts.firstPerson()) || name.equals(ts.secondPerson())) {
				c++;
			}
		}
		assetEquals(2, c);
	}

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
