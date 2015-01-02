package com.nc.gs.serializers.java.util.opt;

import static symbols.io.abstraction._Tags.Serializer.NULL;

import java.util.Iterator;
import java.util.Set;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.serializers.java.util.CollectionSerializer;

@SuppressWarnings("unchecked")
public final class SSMulti {

	private static Object IC_R(Context c, Source src) {
		return null;
	}

	private static void IC_W(Context c, Sink dst, Object v) {

	}

	static Instantiator ctor;

	static int SHAPE;

	public void inflateData0(Context c, Source src, Object o) {
		Set<Object> s = (Set<Object>) o;

		int sz = src.readVarInt();

		for (int i = 0; i < sz; i++) {
			s.add(IC_R(c, src));
		}
	}

	public void inflateData1(Context c, Source src, Object o) {
		Set<Object> s = (Set<Object>) o;

		CollectionSerializer.readExtensions(c, src, s, ctor, SHAPE);

		int sz = src.readVarInt();

		if (sz != 0) {

			int n = src.readInt();

			for (int i = 0; i < sz; i++) {
				if (i == n) {
					s.add(null);
				} else {
					s.add(IC_R(c, src));
				}
			}
		}

	}

	public Object instantiateKCMP(Source src) {
		Object rv;
		src.mark();

		int v = src.readVarInt();

		rv = v == NULL ? ctor.allocate() : ctor.allocateHollow();

		src.reset();

		return rv;
	}

	public Object instantiateKSZ(Source src) {
		src.mark();

		int sz = src.readVarInt();

		src.reset();

		return ctor.allocate(sz);
	}

	public Object instantiateU(Source src) {
		Object rv;

		Instantiator ctor = SerializerFactory.instantiatorOf(Context.rawReadType(src));

		boolean sorted = src.readByte() == ObjectShape.SORTED;

		src.mark();

		int v = src.readVarInt();

		if (sorted) {
			rv = v == NULL ? ctor.allocate() : ctor.allocateHollow();
		} else {
			rv = ctor.allocate(v);
		}

		src.reset();

		return rv;
	}

	public void writeData0(Context c, Sink dst, Object o) {
		Set<Object> s = (Set<Object>) o;

		CollectionSerializer.writeExtensions(c, dst, s, SHAPE);

		int sz = s.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			Iterator<Object> itr = s.iterator();

			for (int i = 0; i < sz; i++) {
				Object v = itr.next();
				IC_W(c, dst, v);
			}
		}
	}

	public void writeData1(Context c, Sink dst, Object o) {
		Set<Object> s = (Set<Object>) o;

		CollectionSerializer.writeExtensions(c, dst, s, SHAPE);

		int sz = s.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			int n = -1;
			int p = dst.doPutInt(n) - 4;

			Iterator<Object> itr = s.iterator();

			for (int i = 0; i < sz; i++) {
				Object v = itr.next();
				if (v == null) {
					n = i;
				} else {
					IC_W(c, dst, v);
				}
			}

			if (n > 0) {
				dst.writeInt(p, n);
			}
		}
	}

}