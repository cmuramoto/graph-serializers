package com.nc.gs.serializers.java.util.opt;

import static symbols.io.abstraction._Tags.Serializer.NULL;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.serializers.java.util.MapSerializer;

@SuppressWarnings("unchecked")
public class MSMulti {

	private static Object ICK_R(Context c, Source src) {
		return null;
	}

	private static void ICK_W(Context c, Sink dst, Object k) {

	}

	private static Object ICV_R(Context c, Source src) {
		return null;
	}

	private static void ICV_W(Context c, Sink dst, Object v) {

	}

	static Instantiator ctor;

	public void inflateData0X0X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readVarInt();

		for (int i = 0; i < sz; i++) {
			m.put(ICK_R(c, src), ICV_R(c, src));
		}
	}

	public void inflateData0X1X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readVarInt();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				m.put(ICK_R(c, src), (fl & 1L << j) == 0 ? null : ICV_R(c, src));
			}
		}
	}

	public void inflateData1X0X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readVarInt();

		int nkAt = src.readInt();

		for (int i = 0; i < sz; i++) {
			m.put(i == nkAt ? null : ICK_R(c, src), ICV_R(c, src));
		}
	}

	public void inflateData1X1X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readVarInt();

		int nkAt = src.readInt();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int base = i << 6;
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				m.put(base + j == nkAt ? null : ICK_R(c, src), (fl & 1L << j) == 0 ? null : ICV_R(c, src));
			}
		}
	}

	public Object instantiateCMP(Source src) {
		Object rv;
		src.mark();

		int v = src.readVarInt();

		rv = v == NULL ? ctor.allocate() : ctor.allocateHollow();

		src.reset();

		return rv;
	}

	public Object instantiateSZ(Source src) {
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

	public void writeData0X0X(Context c, Sink dst, Object o) {
		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeVarInt(sz);

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		for (int i = 0; i < sz; i++) {
			Entry<Object, Object> e = itr.next();
			Object k = e.getKey();
			Object v = e.getValue();

			ICK_W(c, dst, k);

			ICV_W(c, dst, v);
		}
	}

	public void writeData0X1X(Context c, Sink dst, Object o) {
		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeVarInt(sz);

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			long fl = 0;
			int max = i != loops ? 64 : sz & 63;
			int pos = dst.reserveMask(max);

			for (int j = 0; j < max; j++) {
				Entry<Object, Object> next = itr.next();
				Object k = next.getKey();
				Object v = next.getValue();

				ICK_W(c, dst, k);

				if (v != null) {
					fl |= 1L << j;

					ICV_W(c, dst, v);
				}
			}

			dst.encodeMask(max, pos, fl);
		}
	}

	public void writeData1X0X(Context c, Sink dst, Object o) {
		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeVarInt(sz);

		int n = -1;

		int p = dst.doPutInt(n) - 4;

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		for (int i = 0; i < sz; i++) {
			Entry<Object, Object> e = itr.next();
			Object k = e.getKey();
			Object v = e.getValue();

			if (k == null) {
				n = i;
			} else {
				ICK_W(c, dst, k);
			}

			ICV_W(c, dst, v);
		}

		dst.writeInt(p, n);
	}

	public void writeData1X1X(Context c, Sink dst, Object o) {
		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeVarInt(sz);

		int n = -1;

		int p = dst.doPutInt(n) - 4;

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			int pos = dst.reserveMask(max);
			long fl = 0;

			for (int j = 0; j < max; j++) {
				Entry<Object, Object> next = itr.next();
				Object k = next.getKey();
				Object v = next.getValue();

				if (k == null) {
					n = (i << 6) + j;
				} else {
					ICK_W(c, dst, k);
				}

				if (v != null) {
					fl |= 1L << j;

					ICV_W(c, dst, v);
				}
			}

			dst.encodeMask(max, pos, fl);
		}

		dst.writeInt(p, n);
	}

}
