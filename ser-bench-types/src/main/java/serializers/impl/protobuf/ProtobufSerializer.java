package serializers.impl.protobuf;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import serializers.spi.CheckingObjectSerializer;
import domain.protobuf.MediaContentHolder.Image;
import domain.protobuf.MediaContentHolder.Image.Size;
import domain.protobuf.MediaContentHolder.Media;
import domain.protobuf.MediaContentHolder.Media.Player;
import domain.protobuf.MediaContentHolder.MediaContent;
import domain.protobuf.MediaContentHolder.Person;

public class ProtobufSerializer extends CheckingObjectSerializer<MediaContent> {

	@Override
	public void checkAllFields(MediaContent content) {
		checkMediaField(content);
		List<Image> list = content.getImageList();
		assetEquals(2, list.size());

		Image image = list.get(0);
		assetEquals(image.getUri(), ts.largeImageUrl());
		assetEquals(image.getSize(), Size.LARGE);
		assetEquals(image.getTitle(), ts.tag());
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);

		image = list.get(1);
		assetEquals(image.getUri(), ts.smallImageUrl());
		assetEquals(image.getSize(), Size.SMALL);
		assetEquals(image.getTitle(), ts.tag());
		assetEquals(image.getWidth(), 0);
		assetEquals(image.getHeight(), 0);
	}

	@Override
	public void checkMediaField(MediaContent content) {
		Media media = content.getMedia();
		assetEquals(media.getUri(), ts.mediaUrl());
		assetEquals(media.getFormat(), "video/mpg4");
		assetEquals(media.getTitle(), ts.tag());
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
			if (name.equals(ts.firstPerson()) || name.equals(ts.secondPerson())) {
				c++;
			}
		}
		assetEquals(2, c);
	}

	@Override
	public MediaContent create() {
		MediaContent contentProto = MediaContent.newBuilder().setMedia(Media.newBuilder().clearCopyright().setFormat("video/mpg4").setPlayer(Player.JAVA).setTitle(ts.tag()).setUri(ts.mediaUrl()).setDuration(1234567).setSize(123).setHeight(0).setWidth(0).setBitrate(0).addPerson(Person.newBuilder().setName(ts.firstPerson())).addPerson(Person.newBuilder().setName(ts.secondPerson())).build())
				.addImage(Image.newBuilder().setHeight(0).setTitle(ts.tag()).setUri(ts.largeImageUrl()).setWidth(0).setSize(Size.LARGE).build()).addImage(Image.newBuilder().setHeight(0).setTitle(ts.tag()).setUri(ts.smallImageUrl()).setWidth(0).setSize(Size.SMALL).build()).build();
		return contentProto;
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		return MediaContent.parseFrom(array);
	}

	@Override
	public String getName() {
		return MessageFormat.format("protobuf ({0})", ts.getClass().getSimpleName());
	}

	@Override
	public byte[] serialize(MediaContent content) throws IOException {
		return content.toByteArray();
	}
}
