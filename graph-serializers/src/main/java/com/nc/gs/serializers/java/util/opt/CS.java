package com.nc.gs.serializers.java.util.opt;

import java.util.Collection;
import java.util.Iterator;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.serializers.java.util.CollectionSerializer;

@SuppressWarnings("unchecked")
public final class CS {

	static Instantiator ctor;
	static GraphSerializer gs;
	static int SHAPE;

	public void inflateData0(Context c, Source src, Object o) {
		GraphSerializer gs = CS.gs;

		Collection<Object> col = (Collection<Object>) o;

		CollectionSerializer.readExtensions(c, src, col, ctor, SHAPE);

		int sz = src.readVarInt();

		for (int i = 0; i < sz; i++) {
			col.add(gs.read(c, src));
		}
	}

	public void inflateData1(Context c, Source src, Object o) {
		GraphSerializer gs = CS.gs;
		Collection<Object> col = (Collection<Object>) o;

		CollectionSerializer.readExtensions(c, src, col, ctor, SHAPE);

		int sz = src.readVarInt();

		if (sz == 0) {
			return;
		}

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				if ((fl & 1L << j) != 0) {
					col.add(gs.read(c, src));
				} else {
					col.add(null);
				}
			}

		}
	}

	public Object instantiateKCMP(Source src) {
		Object rv;

		// boolean sorted = (SHAPE & ObjectShape.SORTED) != 0;

		src.mark();

		int v = src.readVarInt();

		// if (sorted) {
		rv = v == ObjectShape.NON_SORTED ? ctor.allocate() : ctor.allocateHollow();
		// } else {
		// rv = ctor.allocate(v);
		// }

		src.reset();

		return rv;
	}

	public Object instantiateKSZ(Source src) {
		src.mark();
		Object rv = ctor.allocate(src.readVarInt());
		src.reset();

		return rv;
	}

	public Object instantiateU(Source src) {
		Object rv;

		Instantiator ctor = SerializerFactory.instantiatorOf(Context.rawReadType(src));

		src.mark();

		int k = src.readVarInt();

		boolean sorted = (k & ObjectShape.SORTED) != 0;

		k = src.readVarInt();

		if (sorted) {
			rv = k == ObjectShape.NON_SORTED ? ctor.allocate() : ctor.allocateHollow();
		} else {
			rv = ctor.allocate(k);
		}

		src.reset();

		return rv;
	}

	public void writeData0(Context c, Sink dst, Object o) {
		GraphSerializer gs = CS.gs;
		Collection<Object> col = (Collection<Object>) o;

		CollectionSerializer.writeExtensions(c, dst, col, SHAPE);

		int sz = col.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			Iterator<Object> left = col.iterator();

			for (int i = 0; i < sz; i++) {
				gs.write(c, dst, left.next());
			}
		}
	}

	public void writeData1(Context c, Sink dst, Object o) {
		GraphSerializer gs = CS.gs;
		Collection<Object> col = (Collection<Object>) o;

		CollectionSerializer.writeExtensions(c, dst, col, SHAPE);

		int sz = col.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			int loops = sz >>> 6;
			int l = (sz & 63) == 0 ? loops : loops + 1;

			Iterator<Object> left = col.iterator();

			Object v;

			for (int i = 0; i < l; i++) {
				int max = i != loops ? 64 : sz & 63;
				long fl = 0L;

				int pos = dst.reserveMask(max);

				for (int j = 0; j < max; j++) {
					v = left.next();

					if (v != null) {
						fl |= 1L << j;
						gs.write(c, dst, v);
					}
				}

				dst.encodeMask(max, pos, fl);
			}
		}
	}
}