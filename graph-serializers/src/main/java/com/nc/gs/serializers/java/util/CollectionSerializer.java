package com.nc.gs.serializers.java.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class CollectionSerializer extends GraphSerializer {

	public static void readExtensions(Context c, Source src, Collection<?> m, Instantiator ctor, int s) {

		int k = ctor != null ? s : src.readVarInt();

		if ((k & ObjectShape.SORTED) != 0) {
			src.mark();
			if (src.readByte() != ObjectShape.NON_SORTED) {
				src.reset();
				Comparator<?> cmp = (Comparator<?>) ((s & ObjectShape.ONLY_PAYLOAD) != 0 ? c.readTypeAndData(src) : c.readRefAndData(src));

				if (ctor == null) {
					ctor = c.instantiatorOf(m.getClass());
				}

				if ((k & ObjectShape.SIZED) != 0) {
					ctor.copy(ctor.allocate(src.readVarInt(), cmp), m);
				} else {
					ctor.copy(ctor.allocate(cmp), m);
				}
			}
		}
	}

	static void tagAndWrite(Context c, Sink dst, Iterator<Object> itr, boolean op, int lim) {
		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		int pos = dst.reserveMask(lim);

		long fl = 0L;

		Object v;

		for (int j = 0; j < lim; j++) {
			v = itr.next();
			if (v != null) {
				fl |= 1L << j;
				Class<? extends Object> clazz = c.writeType(dst, v);

				if (clazz == pc) {
					gs = ps;
				} else {
					pc = clazz;
					gs = ps = c.forType(clazz);
				}
				if (op) {
					gs.writeData(c, dst, v);
				} else {
					try {
						gs.write(c, dst, v);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}

		dst.encodeMask(lim, pos, fl);
	}

	static void tagAndWrite(Context c, Sink dst, Iterator<Object> itr, GraphSerializer gs, boolean op, int lim) {
		long fl = 0L;

		int pos = dst.reserveMask(lim);

		for (int j = 0; j < lim; j++) {
			Object v = itr.next();

			if (v != null) {
				fl |= 1L << j;

				if (op) {
					gs.writeData(c, dst, v);
				} else {
					gs.write(c, dst, v);
				}
			}
		}

		dst.encodeMask(lim, pos, fl);
	}

	static void untagAndRead(Context c, Source src, Collection<Object> col, boolean op, long fl, int lim) {

		GraphSerializer gs;
		Class<?> pc = null;
		GraphSerializer ps = null;

		for (int j = 0; j < lim; j++) {
			if ((fl & 1L << j) != 0) {
				Class<?> clazz = c.readType(src);

				if (clazz == pc) {
					gs = ps;
				} else {
					pc = clazz;
					gs = ps = c.forType(clazz);
				}

				col.add(op ? gs.readData(c, src) : gs.read(c, src));
			} else {
				col.add(null);
			}
		}

	}

	static void untagAndRead(Context c, Source src, Collection<Object> col, GraphSerializer gs, boolean op, long fl, int lim) {
		for (int j = 0; j < lim; j++) {
			if ((fl & 1L << j) != 0) {
				col.add(op ? gs.readData(c, src) : gs.read(c, src));
			} else {
				col.add(null);
			}
		}
	}

	public static void writeExtensions(Context c, Sink dst, Collection<?> o, int s) {

		int k;
		Object state;

		if (s < 0) {
			Shape shape = Shape.of(o);
			state = shape.state;
			k = shape.k;
			Context.rawWriteReplacing(dst, o.getClass());
			dst.writeVarInt(k);

		} else {
			k = s;
			state = Shape.state(s, o);
		}

		if ((k & ObjectShape.SORTED) != 0) {
			if (state == null) {
				dst.writeByte(ObjectShape.NON_SORTED);
			} else {
				if ((s & ObjectShape.ONLY_PAYLOAD) != 0) {
					c.writeTypeAndData(dst, state);
				} else {
					c.writeRefAndData(dst, state);
				}

				if ((k & ObjectShape.SIZED) != 0) {
					dst.writeVarInt(o.size());
				}
			}
		}
	}

	public static final CollectionSerializer NO_REFS = new CollectionSerializer(null, null, false, false);

	public static final CollectionSerializer NO_REFS_NON_NULL = new CollectionSerializer(null, null, false, true);

	public static final CollectionSerializer WITH_REFS = new CollectionSerializer(null, null, true, false);

	public static final CollectionSerializer WITH_REFS_NON_NULL = new CollectionSerializer(null, null, true, true);

	final Instantiator ctor;

	final GraphSerializer gs;

	final int s;

	public CollectionSerializer(Class<? extends Collection<?>> colType, Class<?> compType, boolean n, boolean op) {
		gs = SerializerFactory.nullIfNotConcrete(compType);
		ctor = SerializerFactory.instantiatorOf(colType);
		s = (n ? ObjectShape.NULLABLE : 0) | (op ? ObjectShape.ONLY_PAYLOAD : 0) | Shape.of(colType);
	}

	@Override
	public void inflateData(Context c, Source src, Object o) {
		@SuppressWarnings("unchecked")
		Collection<Object> col = (Collection<Object>) o;

		readExtensions(c, src, col, ctor, s);

		int sz = src.readVarInt();

		if (sz > 0) {
			if ((s & ObjectShape.NULLABLE) != 0) {
				readBitMask(c, src, col, sz);
			} else {
				readEls(c, src, col, sz);
			}
		}
	}

	@Override
	public Object instantiate(Source src) {
		Object rv;
		Instantiator ctor = this.ctor;

		boolean sorted;

		int v;

		if (ctor == null) {
			ctor = SerializerFactory.instantiatorOf(Context.rawReadType(src));

			src.mark();

			int k = src.readVarInt();

			sorted = (k & ObjectShape.SORTED) != 0;

			v = src.readVarInt();
		} else {
			sorted = (s & ObjectShape.SORTED) != 0;

			src.mark();

			v = src.readVarInt();
		}

		if (sorted) {
			rv = v == ObjectShape.NON_SORTED ? ctor.allocate() : ctor.allocateHollow();
		} else {
			rv = ctor.allocate(v);
		}

		src.reset();

		return rv;
	}

	private void readBitMask(Context c, Source src, Collection<Object> col, int sz) {
		GraphSerializer gs = this.gs;
		boolean op = (s & ObjectShape.ONLY_PAYLOAD) != 0;

		int loops = sz >>> 6;
			int l = (sz & 63) == 0 ? loops : loops + 1;

			if (gs != null) {
				for (int i = 0; i < l; i++) {
					int max = i != loops ? 64 : sz & 63;
					untagAndRead(c, src, col, gs, op, src.decodeMask(max), max);
				}
			} else {
				for (int i = 0; i < l; i++) {
					int max = i != loops ? 64 : sz & 63;
					untagAndRead(c, src, col, op, src.decodeMask(max), max);
				}
			}
	}

	private void readEls(Context c, Source src, Collection<Object> col, int sz) {
		GraphSerializer gs = this.gs;
		boolean op = (s & ObjectShape.ONLY_PAYLOAD) != 0;

		if (gs != null) {
			if (op) {
				for (int i = 0; i < sz; i++) {
					col.add(gs.readData(c, src));
				}
			} else {
				for (int i = 0; i < sz; i++) {
					col.add(gs.read(c, src));
				}
			}
		} else {
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

					col.add(gs.readData(c, src));
				}
			} else {
				for (int i = 0; i < sz; i++) {
					col.add(c.readRefAndData(src));
				}
			}

		}
	}

	@Override
	public String toString() {
		return String.format("CollectionSerializer [n:%s,op=%s]", (s & ObjectShape.NULLABLE) != 0, (s & ObjectShape.ONLY_PAYLOAD) != 0);
	}

	private void writeBitMask(Context c, Sink dst, int len, Iterator<Object> itr) {
		GraphSerializer gs = this.gs;
		boolean op = (s & ObjectShape.ONLY_PAYLOAD) != 0;

		int loops = len >>> 6;
				int l = (len & 63) == 0 ? loops : loops + 1;

				if (gs != null) {
					for (int i = 0; i < l; i++) {
						tagAndWrite(c, dst, itr, gs, op, i != loops ? 64 : len & 63);
					}
				} else {
					for (int i = 0; i < l; i++) {
						tagAndWrite(c, dst, itr, op, i != loops ? 64 : len & 63);
					}
				}

	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		@SuppressWarnings("unchecked")
		Collection<Object> col = (Collection<Object>) o;

		writeExtensions(c, dst, col, s);

		int sz = col.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			Iterator<Object> itr = col.iterator();
			if ((s & ObjectShape.NULLABLE) != 0) {
				writeBitMask(c, dst, sz, itr);
			} else {
				writeEls(c, dst, sz, itr);
			}
		}
	}

	private void writeEls(Context c, Sink dst, int sz, Iterator<Object> left) {
		GraphSerializer gs = this.gs;
		boolean op = (s & ObjectShape.ONLY_PAYLOAD) != 0;

		if (gs != null) {
			for (int i = 0; i < sz; i++) {
				Object v = left.next();
				if (op) {
					gs.writeData(c, dst, v);
				} else {
					gs.write(c, dst, v);
				}
			}
		} else {
			Class<?> pc = null;
			GraphSerializer ps = null;

			for (int i = 0; i < sz; i++) {
				Object v = left.next();

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
}