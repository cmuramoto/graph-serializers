package com.nc.gs.core;

import static symbols.io.abstraction._Tags.Serializer.NULL;
import static symbols.io.abstraction._Tags.Serializer.NULL_I;
import static symbols.io.abstraction._Tags.Serializer.TYPE_ID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.log.Log;

public final class Serializer {

	@SuppressWarnings("restriction")
	private static void clean(MappedByteBuffer buff) {
		try {
			if (buff instanceof sun.nio.ch.DirectBuffer) {
				sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buff).cleaner();
				if (cleaner != null) {
					cleaner.clean();
				}
			}
		} catch (Throwable e) {
			Log.error(e);
		}
	}

	public static <T> void inflateRoot(DataInput in, T instance) throws IOException {
		try (Context c = Context.reading()) {
			c.inflate(in, instance);
		}
	}

	public static final Object readNested(Context c, Source src) {
		Object rv;

		int id = src.readVarInt();

		if (id == NULL_I) {
			rv = null;
		} else if (id == TYPE_ID) {
			Class<?> type = c.readType(src);
			id = src.readVarInt();
			GraphSerializer serializer = c.forType(type);
			rv = serializer.instantiate(src);
			c.mark(rv, id);
			serializer.inflateData(c, src, rv);
		} else {
			rv = c.from(id);
		}

		return rv;
	}

	public static <T> T readRoot(DataInput in) throws IOException {
		try (Context c = Context.reading()) {
			return c.read(in);
		}
	}

	public static <T> T readRoot(File file, Class<T> type) {
		MappedByteBuffer buff = null;
		try (Source s = Source.mmap(file)) {
			return readRoot(s, type);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			clean(buff);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T readRoot(Source src, Class<T> type) {
		GraphSerializer gs = SerializerFactory.serializer(type);

		try (Context c = Context.reading()) {
			return (T) gs.readRoot(c, src);
		}
	}

	/**
	 * Defines a reliable way of writing an object, without any prior knowledge regarding it's type
	 * or state (possibly being null). This method must be compensated by
	 * {@link GraphSerializer#readNested(Context, ByteBuffer)}.
	 *
	 * @param c
	 * @param dst
	 * @param o
	 */
	public static final void writeNested(Context c, Sink dst, Object o) {
		if (o == null) {
			dst.write(NULL);
			return;
		}

		GraphSerializer gs = c.forNested(dst, o);

		if (gs != null) {
			gs.writeData(c, dst, o);
		}
	}

	public static <T> void writeRoot(DataOutput out, T o, boolean wt) throws IOException {
		try (Context c = Context.writing()) {
			c.write(out, o, wt);
		}
	}

	public static <T> void writeRoot(File file, T o) throws IOException {
		writeRoot(file, o, 1024 * 1024 * 10);
	}

	public static <T> void writeRoot(File file, T o, int estimatedSize) {
		try (Sink s = Sink.mmap(file)) {
			writeRoot(s, o);
		}
	}

	public static void writeRoot(Sink dst, Object o) {
		GraphSerializer gs = SerializerFactory.serializer(o.getClass());

		try (Context c = Context.writing()) {
			gs.writeRoot(c, dst, o);
		}
	}

}