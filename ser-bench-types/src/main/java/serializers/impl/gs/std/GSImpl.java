package serializers.impl.gs.std;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import serializers.impl.gs.P;
import serializers.spi.CheckingObjectSerializer;

import com.nc.gs.core.Genesis;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;

import domain.gs.std.Image;
import domain.gs.std.Image.Size;
import domain.gs.std.Media;
import domain.gs.std.Media.Player;
import domain.gs.std.MediaContent;
import domain.gs.std.Person;

public class GSImpl extends CheckingObjectSerializer<MediaContent> {

	static P borrow() {
		return P;
	}

	static final P P = new P();

	static {
		Genesis.bootstrap();
		gs = SerializerFactory.serializer(MediaContent.class);
	}

	static final GraphSerializer gs;

	// static ConcurrentLinkedQueue<P> buffers = new ConcurrentLinkedQueue<>();
	static LinkedList<P> buffers = new LinkedList<>();

	public GSImpl() {
	}

	@Override
	public void checkAllFields(MediaContent content) {
		checkMediaField(content);
		List<Image> list = content.getImages();
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

		List<Person> list = media.getPersons();
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
	public MediaContent create() throws Exception {
		Media media = new Media(null, "video/mpg4", Media.Player.JAVA, ts.tag(), ts.mediaUrl(), 1234567, 123, 0, 0, 0);
		media.addToPerson(ts.firstPerson());
		media.addToPerson(ts.secondPerson());
		MediaContent content = new MediaContent(media);
		content.addImage(new Image(0, ts.tag(), ts.largeImageUrl(), 0, Image.Size.LARGE));
		content.addImage(new Image(0, ts.tag(), ts.smallImageUrl(), 0, Image.Size.SMALL));
		return content;
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		try (P p = borrow()) {
			return (MediaContent) gs.readRoot(p.reading(), p.src.filledWith(array));
		}
	}

	@Override
	public String getName() {
		return MessageFormat.format("graph-ser ({0})", ts.getClass().getSimpleName());
	}

	@Override
	public byte[] serialize(MediaContent content) throws Exception {
		byte[] rv;
		try (P p = borrow()) {
			gs.writeRoot(p.writing(), p.dst, content);
			rv = p.dst.toByteArray();
		}
		return rv;
	}
}
