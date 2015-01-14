package com.nc.gs.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.MappedByteBuffer;

import com.nc.gs.io.Source;
import com.nc.gs.log.Log;

public final class Serializer {

	@SuppressWarnings("restriction")
	static void clean(MappedByteBuffer buff) {
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

	public static <T> T readRoot(File file, Class<T> type) {
		try (Context c = Context.reading(); FileInputStream fis = new FileInputStream(file)) {
			return c.read(fis, type);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T readRoot(Source src, Class<T> type) {
		GraphSerializer gs = SerializerFactory.serializer(type);

		try (Context c = Context.reading()) {
			return (T) gs.readRoot(c, src);
		}
	}

	public static <T> void writeRoot(File file, T o) {
		writeRoot(file, o, false);
	}

	public static <T> void writeRoot(File file, T o, boolean wt) {
		try (Context c = Context.writing(); FileOutputStream fos = new FileOutputStream(file)) {
			c.write(fos, o, wt);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}