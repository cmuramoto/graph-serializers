package serializers.impl.gs;

import java.util.LinkedList;
import java.util.List;

import serializers.spi.CheckingObjectSerializer;

import com.nc.gs.core.Context;
import com.nc.gs.core.Genesis;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

import domain.gs.Image;
import domain.gs.Image.Size;
import domain.gs.Media;
import domain.gs.Media.Player;
import domain.gs.MediaContent;
import domain.gs.Person;

public class GSImpl implements CheckingObjectSerializer<MediaContent> {

	static class P {
		Sink sink;
		Source source;

		P() {
			this.sink = new Sink(4096);
			this.source = new Source(4096);
		}

		public void clear() {
			sink.clear();
			source.clear();
		}
	}

	static P borrow() {
		// P rv = C;
		//
		// if (rv == null) {
		// rv = buffers.poll();
		//
		// if (rv == null) {
		// rv = new P();
		// }
		// }
		//
		// return rv;

		return C;
	}

	public static void writeRoot(Sink dst, Object o) {
		GraphSerializer gs = SerializerFactory.serializer(o.getClass());

		try (Context c = Context.writing()) {
			gs.writeRoot(c, dst, o);
		}
	}

	static P C = new P();

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

		List<Person> list = media.getPersons();
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
	public MediaContent create() throws Exception {
		Media media = new Media(null, "video/mpg4", Media.Player.JAVA, "Javaone Keynote", "http://javaone.com/keynote.mpg", 1234567, 123, 0, 0, 0);
		media.addToPerson("Bill Gates");
		media.addToPerson("Steve Jobs");
		MediaContent content = new MediaContent(media);
		content.addImage(new Image(0, "Javaone Keynote", "http://javaone.com/keynote_large.jpg", 0, Image.Size.LARGE));
		content.addImage(new Image(0, "Javaone Keynote", "http://javaone.com/keynote_thumbnail.jpg", 0, Image.Size.SMALL));
		return content;
	}

	@Override
	public MediaContent deserialize(byte[] array) throws Exception {
		P p = borrow();
		try (Context c = Context.reading()) {
			return (MediaContent) gs.readRoot(c, p.source.filledWith(array));
		} finally {
			restoreSource(p);
		}
	}

	@Override
	public String getName() {
		return "graph-ser";
	}

	private void restoreSink(P p) {
		p.sink.clear();
		// if (C == null) {
		// C = p;
		// } else {
		// buffers.offer(p);
		// }

		// buffers.offer(p);
	}

	private void restoreSource(P p) {
		p.source.clear();
		// if (C == null) {
		// C = p;
		// } else {
		// buffers.offer(p);
		// }

		// buffers.offer(p);
	}

	@Override
	public byte[] serialize(MediaContent content) throws Exception {
		byte[] rv;
		P p = borrow();
		writeRoot(p.sink, content);
		rv = p.sink.toByteArray();
		restoreSink(p);
		return rv;
	}
}
