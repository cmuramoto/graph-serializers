package com.nc.gs.serializers.java.util;

import java.util.Iterator;
import java.util.Set;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.generator.InstantiatorAdapter;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class SetSerializer extends GraphSerializer {

	public static SetSerializer basic() {
		SetSerializer rv = BASIC;
		if (rv == null) {
			rv = BASIC = new SetSerializer(null, null, true, false);
		}
		return rv;
	}

	static SetSerializer BASIC;

	// public static void readExtensions(Context c, ByteBuffer src, Object m,
	// Instantiator ctor,
	// boolean op) {
	// if (m instanceof SortedSet) {
	// src.mark();
	// if (src.get() != Serializer.NULL) {
	// src.reset();
	// Comparator<?> cmp = (Comparator<?>) (op ? c.readTypeAndData(src) :
	// c.readRefAndData(src));
	//
	// if (ctor == null) {
	// ctor = c.instantiatorOf(m.getClass());
	// }
	//
	// ctor.copy(ctor.allocate(cmp), m);
	// }
	// }
	// }

	// public static void writeExtensions(Context c, ByteBuffer dst, Set<?> s,
	// boolean wt, boolean
	// op) {
	//
	// Shape shape = Shape.of(s);
	// Object state = shape.state;
	//
	// if (wt) {
	// Context.rawWriteType(dst, s.getClass());
	// dst.put(shape.sorted());
	// }
	//
	// if (s instanceof SortedSet) {
	// if (state == null) {
	// dst.put(Serializer.NULL);
	// } else {
	// if (op) {
	// c.writeTypeAndData(dst, state);
	// } else {
	// c.writeRefAndData(dst, state);
	// }
	// }
	// }
	// }

	final Instantiator ctor;
	final Class<?> type;

	final GraphSerializer gs;

	final int s;

	public SetSerializer(Class<? extends Set<?>> setType, Class<?> compType, boolean n, boolean op) {
		super();
		gs = SerializerFactory.nullIfNotConcrete(compType);
		setType = Utils.nullIfNotConcrete(setType);

		type = setType;
		s = (n ? ObjectShape.NULLABLE : 0) | (op ? ObjectShape.ONLY_PAYLOAD : 0) | Shape.of(setType);
		ctor = setType == null ? null : InstantiatorAdapter.of(setType);
	}

	@Override
	public void inflateData(Context c, Source src, Object o) {
		@SuppressWarnings("unchecked")
		Set<Object> s = (Set<Object>) o;

		// readExtensions(c, src, s, ctor, (s & 0x2) != 0);

		CollectionSerializer.readExtensions(c, src, s, ctor, this.s);

		int sz = src.readVarInt();

		if (sz == 0) {
			return;
		}

		GraphSerializer gs = this.gs;
		boolean op = (this.s & ObjectShape.ONLY_PAYLOAD) != 0;

		int nullAt = (this.s & ObjectShape.NULLABLE) != 0 ? src.readInt() : -1;

		if (gs != null) {
			for (int ix = 0; ix < sz; ix++) {
				if (ix == nullAt) {
					s.add(null);
				} else {
					Object v = op ? gs.readData(c, src) : gs.read(c, src);
					s.add(v);
				}
			}
		} else {
			Class<?> pc = null;
			GraphSerializer ps = null;
			GraphSerializer ser;

			for (int ix = 0; ix < sz; ix++) {
				if (ix == nullAt) {
					s.add(null);
				} else {
					Class<?> clazz = c.readType(src);
					if (clazz == pc) {
						ser = ps;
					} else {
						pc = clazz;
						ser = ps = c.forType(clazz);
					}

					if (op) {
						s.add(ser.readData(c, src));
					} else {
						s.add(ser.read(c, src));
					}
				}
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

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		GraphSerializer gs = this.gs;

		@SuppressWarnings("unchecked")
		Set<Object> s = (Set<Object>) o;
		boolean op = (this.s & ObjectShape.ONLY_PAYLOAD) != 0;

		int sz = s.size();
		int nullAt = -1;

		CollectionSerializer.writeExtensions(c, dst, s, this.s);
		// writeExtensions(c, dst, s, ctor == null, op);

		dst.writeVarInt(sz);

		if (sz == 0) {
			return;
		}

		Iterator<Object> itr = s.iterator();

		int pos = (this.s & ObjectShape.NULLABLE) != 0 ? dst.doPutInt(nullAt) - 4 : -1;

		if (gs != null) {
			for (int i = 0; i < sz; i++) {
				Object v = itr.next();
				if (v == null) {
					nullAt = i;
				} else {
					if (op) {
						gs.writeData(c, dst, v);
					} else {
						gs.write(c, dst, v);
					}
				}
			}
		} else {
			Class<?> pc = null;
			GraphSerializer ps = null;

			for (int i = 0; i < sz; i++) {
				Object v = itr.next();
				if (v == null) {
					nullAt = i;
				} else {
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
						gs.write(c, dst, v);
					}
				}
			}
		}

		if (pos > 0) {
			dst.writeInt(pos, nullAt);
		}
	}
}