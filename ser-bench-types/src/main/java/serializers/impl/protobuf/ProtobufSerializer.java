package serializers.impl.protobuf;

import java.io.IOException;
import java.util.List;

import serializers.spi.CheckingObjectSerializer;
import domain.protobuf.MediaContentHolder.Image;
import domain.protobuf.MediaContentHolder.Image.Size;
import domain.protobuf.MediaContentHolder.Media;
import domain.protobuf.MediaContentHolder.Media.Player;
import domain.protobuf.MediaContentHolder.MediaContent;
import domain.protobuf.MediaContentHolder.Person;

public class ProtobufSerializer implements CheckingObjectSerializer<MediaContent> {

	@Override
	public void checkAllFields(MediaContent content) {
		checkMediaField(content);
		List<Image> list = content.getImageList();
		assetEquals(2, list.size());

		Image image = list.get(0);
		assetEquals(image.getUri(), "http://javaone.com/keynote_large.jpg");
		assetEquals(image.getSize(), Size.LARGE);
		assetEquals(image.getTitle(), "Javaone Keynote");
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);

		image = list.get(1);
		assetEquals(image.getUri(), "http://javaone.com/keynote_thumbnail.jpg");
		assetEquals(image.getSize(), Size.SMALL);
		assetEquals(image.getTitle(), "Javaone Keynote");
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);
	}

	@Override
	public void checkMediaField(MediaContent content) {
		Media media = content.getMedia();
		assetEquals(media.getUri(), "http://javaone.com/keynote.mpg");
		assetEquals(media.getFormat(), "video/mpg4");
		assetEquals(media.getTitle(), "Javaone Keynote");
		assetEquals(media.getDuration(), 1234567L);
		assetEquals(media.getSize(), 123L);
		assetEquals(media.getBitrate(), 0);
		assetEquals(media.getPlayer(), Player.JAVA);
		assetEquals(media.getWidth(), 0);
		assetEquals(media.getHeight(), 0);

		List<Person> list = media.getPersonList();
		// assetEquals(2, list.size());
		int c = 0;
		for (Person person : list) {
			String name = person.getName();
			if (name.equals("Bill Gates") || name.equals("Steve Jobs")) {
				c++;
			}
		}
		assetEquals(2, c);
	}

	@Override
	public MediaContent create() {
		MediaContent contentProto = MediaContent.newBuilder()
				.setMedia(Media.newBuilder().clearCopyright().setFormat("video/mpg4").setPlayer(Player.JAVA).setTitle("Javaone Keynote").setUri("http://javaone.com/keynote.mpg").setDuration(1234567).setSize(123).setHeight(0).setWidth(0).setBitrate(0).addPerson(Person.newBuilder().setName("Bill Gates")).addPerson(Person.newBuilder().setName("Steve Jobs")).build())
				.addImage(Image.newBuilder().setHeight(0).setTitle("Javaone Keynote").setUri("http://javaone.com/keynote_large.jpg").setWidth(0).setSize(Size.LARGE).build()).addImage(Image.newBuilder().setHeight(0).setTitle("Javaone Keynote").setUri("http://javaone.com/keynote_thumbnail.jpg").setWidth(0).setSize(Size.SMALL).build()).build();
		return contentProto;
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		return MediaContent.parseFrom(array);
	}

	@Override
	public String getName() {
		return "protobuf";
	}

	@Override
	public byte[] serialize(MediaContent content) throws IOException {
		return content.toByteArray();
	}
}
