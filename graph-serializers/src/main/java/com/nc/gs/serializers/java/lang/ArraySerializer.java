package com.nc.gs.serializers.java.lang;

import java.lang.reflect.Array;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class ArraySerializer extends GraphSerializer {

	public static ArraySerializer forShape(Shape shape) {
		boolean op = shape.disregardRefs();

		return shape.canBeNull() ? op ? NULL_NO_REFS : NULL_WITH_REFS : op ? NOT_NULL_NO_REFS : NOT_NULL_WITH_REFS;
	}

	public static ArraySerializer NULL_WITH_REFS = new ArraySerializer(true, false);

	public static ArraySerializer NULL_NO_REFS = new ArraySerializer(true, true);

	public static ArraySerializer NOT_NULL_WITH_REFS = new ArraySerializer(false, false);

	public static ArraySerializer NOT_NULL_NO_REFS = new ArraySerializer(false, true);

	final boolean n;
	final boolean op;

	public ArraySerializer(boolean n, boolean op) {
		this.n = n;
		this.op = op;
	}

	@Override
	public void inflateData(Context c, Source src, Object o) {
		Class<? extends Object> clazz = o.getClass();
		Class<? extends Object> ct = clazz.getComponentType();

		if (ct.isPrimitive()) {
			src.inflatePrimiteArray(Context.typeId(ct), o);
		} else {
			Object[] array = (Object[]) o;

			if (array.length == 0) {
				return;
			}

			if (n) {
				readBitMask(c, src, array);
			} else {
				readEls(c, src, array);
			}
		}
	}

	@Override
	public Object instantiate(Source src) {
		int len = src.readIntP();
		Class<?> ct = Context.rawReadType(src);

		return Array.newInstance(ct, len);
	}

	private void readBitMask(Context c, Source src, Object[] array) {
		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		int sz = array.length;
		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		int ix = 0;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			if (op) {
				for (int j = 0; j < max && ix < sz; j++) {
					if ((fl & 1L << j) != 0) {
						Class<?> clazz = c.readType(src);

						if (clazz == pc) {
							gs = ps;
						} else {
							pc = clazz;
							gs = ps = c.forType(clazz);
						}

						array[ix++] = gs.readData(c, src);
					}
				}
			} else {
				for (int j = 0; j < max && ix < sz; j++) {
					if ((fl & 1L << j) != 0) {
						array[ix++] = c.readRefAndData(src);
					}
				}
			}
		}
	}

	private void readEls(Context c, Source src, Object[] array) {
		int sz = array.length;

		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		if (op) {
			for (int i = 0; i < sz; i++) {
				Class<?> clazz = c.readType(src);

				if (clazz == pc) {
					gs = ps;
				} else {
					pc = clazz;
					gs = ps = c.forType(clazz);
				}

				array[i] = gs.readData(c, src);
			}
		} else {
			for (int i = 0; i < sz; i++) {
				array[i] = c.readRefAndData(src);
			}
		}
	}

	private void writeBitMask(Context c, Sink dst, Object[] array) {
		int len = array.length;
		int loops = len >>> 6;
		int l = (len & 63) == 0 ? loops : loops + 1;
		boolean op = this.op;

		Object v;
		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		int ix = 0;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : len & 63;
			long fl = 0L;

			int pos = dst.reserveMask(max);

			for (int j = 0; j < max; j++) {
				if ((v = array[ix++]) != null) {
					fl |= 1L << j;
					if (v != null) {
						if (op || !c.nullSafeVisited(dst, v)) {
							Class<? extends Object> clazz = c.writeType(dst, v);

							if (clazz == pc) {
								gs = ps;
							} else {
								pc = clazz;
								gs = ps = c.forType(clazz);
							}

							gs.writeData(c, dst, v);
						}
					}
				}
			}

			dst.encodeMask(max, pos, fl);
		}
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		Class<? extends Object> clazz = o.getClass();
		Class<? extends Object> ct = clazz.getComponentType();

		// for instantiate
		dst.writeIntP(Array.getLength(o));
		Context.rawWriteType(dst, ct);

		if (ct.isPrimitive()) {
			dst.writePrimitiveArray(Context.typeId(ct), o);
		} else {
			Object[] array = (Object[]) o;

			if (array.length == 0) {
				return;
			}

			if (n) {
				writeBitMask(c, dst, array);
			} else {
				writeEls(c, dst, array);
			}
		}
	}

	private void writeEls(Context c, Sink dst, Object[] array) {
		int sz = array.length;

		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		if (op) {
			for (int i = 0; i < sz; i++) {
				Object v = array[i];

				Class<? extends Object> clazz = c.writeType(dst, v);

				if (clazz == pc) {
					gs = ps;
				} else {
					pc = clazz;
					gs = ps = c.forType(clazz);
				}

				gs.writeData(c, dst, v);
			}
		} else {
			for (int i = 0; i < sz; i++) {
				Object v = array[i];

				if (!c.nullSafeVisited(dst, v)) {
					Class<? extends Object> clazz = c.writeType(dst, v);

					if (clazz == pc) {
						gs = ps;
					} else {
						pc = clazz;
						gs = ps = c.forType(clazz);
					}

					gs.writeData(c, dst, v);
				}
			}
		}
	}
}