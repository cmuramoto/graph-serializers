package com.nc.gs.serializers.java.util.opt;

import static symbols.io.abstraction._Tags.Serializer.NULL;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.serializers.java.util.MapSerializer;

public class MS {

	static Instantiator ctor;
	static GraphSerializer ks;
	static GraphSerializer vs;

	public void inflateData0X0X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		@SuppressWarnings("unchecked")
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readIntP();

		for (int i = 0; i < sz; i++) {
			m.put(ks.read(c, src), vs.readData(c, src));
		}
	}

	public void inflateData0X1X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		@SuppressWarnings("unchecked")
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readIntP();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				m.put(ks.read(c, src), (fl & 1L << j) == 0 ? null : vs.readData(c, src));
			}
		}
	}

	public void inflateData1X0X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		@SuppressWarnings("unchecked")
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readIntP();

		int nkAt = src.readInt();

		for (int i = 0; i < sz; i++) {
			m.put(i == nkAt ? null : ks.read(c, src), vs.readData(c, src));
		}
	}

	public void inflateData1X1X(Context c, Source src, Object o) {
		MapSerializer.readExtensions(c, src, o, ctor);

		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		@SuppressWarnings("unchecked")
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = src.readIntP();

		int nkAt = src.readInt();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int base = i << 6;
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				m.put(base + j == nkAt ? null : ks.read(c, src), (fl & 1L << j) == 0 ? null : vs.readData(c, src));
			}
		}
	}

	public Object instantiateCMP(Source src) {
		Object rv;
		src.mark();

		int v = src.readIntP();

		rv = v == NULL ? ctor.allocate() : ctor.allocateHollow();

		src.reset();

		return rv;
	}

	public Object instantiateSZ(Source src) {
		src.mark();

		int sz = src.readIntP();

		src.reset();

		return ctor.allocate(sz);
	}

	public Object instantiateU(Source src) {
		Object rv;

		Instantiator ctor = SerializerFactory.instantiatorOf(Context.rawReadType(src));

		boolean sorted = src.readByte() == ObjectShape.SORTED;

		src.mark();

		int v = src.readIntP();

		if (sorted) {
			rv = v == NULL ? ctor.allocate() : ctor.allocateHollow();
		} else {
			rv = ctor.allocate(v);
		}

		src.reset();

		return rv;
	}

	public void writeData0X0X(Context c, Sink dst, Object o) {
		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		MapSerializer.writeExtensions(c, dst, o, true);

		@SuppressWarnings("unchecked")
		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeIntP(sz);

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		for (int i = 0; i < sz; i++) {
			Entry<Object, Object> e = itr.next();
			Object k = e.getKey();
			Object v = e.getValue();

			ks.write(c, dst, k);

			vs.writeData(c, dst, v);
		}
	}

	@SuppressWarnings("unchecked")
	public void writeData0X1X(Context c, Sink dst, Object o) {
		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeIntP(sz);

		Iterator<Entry<Object, Object>> itr = m.entrySet().iterator();

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = 0;
			int pos = dst.reserveMask(max);

			for (int j = 0; j < max; j++) {
				Entry<Object, Object> next = itr.next();
				Object k = next.getKey();
				Object v = next.getValue();

				ks.write(c, dst, k);

				if (v != null) {
					fl |= 1L << j;

					vs.writeData(c, dst, v);
				}
			}

			dst.encodeMask(max, pos, fl);
		}
	}

	@SuppressWarnings("unchecked")
	public void writeData1X0X(Context c, Sink dst, Object o) {
		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeIntP(sz);

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
				ks.write(c, dst, k);
			}

			vs.writeData(c, dst, v);
		}

		dst.writeInt(p, n);
	}

	@SuppressWarnings("unchecked")
	public void writeData1X1X(Context c, Sink dst, Object o) {
		GraphSerializer ks = MS.ks;
		GraphSerializer vs = MS.vs;

		MapSerializer.writeExtensions(c, dst, o, true);

		Map<Object, Object> m = (Map<Object, Object>) o;

		int sz = m.size();

		dst.writeIntP(sz);

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
					ks.write(c, dst, k);
				}

				if (v != null) {
					fl |= 1L << j;

					vs.writeData(c, dst, v);
				}
			}

			dst.encodeMask(max, pos, fl);
		}

		dst.writeInt(p, n);
	}

}