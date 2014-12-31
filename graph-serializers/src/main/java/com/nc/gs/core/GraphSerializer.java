package com.nc.gs.core;

import static symbols.io.abstraction._Tags.Serializer.ID_BASE;

import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public abstract class GraphSerializer {

	public void inflateData(Context c, Source src, Object o) {
	}

	public abstract Object instantiate(Source src);

	public final Object read(Context c, Source src) {
		int id = src.readIntP();
		Object rv = c.from(id);
		if (rv == null) {
			rv = instantiate(src);
			c.mark(rv, id);
			inflateData(c, src, rv);
		}
		return rv;
	}

	public final Object readData(Context c, Source src) {
		Object rv = instantiate(src);

		inflateData(c, src, rv);

		return rv;
	}

	public final Object readRoot(Context c, Source src) {
		Object o = instantiate(src);

		c.mark(o, ID_BASE);

		inflateData(c, src, o);

		return o;
	}

	public final void write(Context c, Sink dst, Object o) {
		if (!c.nullSafeVisited(dst, o)) {
			writeData(c, dst, o);
		}
	}

	/**
	 * Writes only the Object's o payload, disregarding it's type, reference and assuming it to be
	 * non-null.
	 *
	 * @param c
	 * @param dst
	 * @param o
	 */
	public abstract void writeData(Context c, Sink dst, Object o);

	/**
	 * Writes a root of an object graph. This method will not write any metadata to the destination
	 * stream, but will mark the object as visited in memory.
	 *
	 * @param c
	 * @param dst
	 * @param o
	 */
	public final void writeRoot(Context c, Sink dst, Object o) {
		c.mark(o);

		writeData(c, dst, o);
	}
}