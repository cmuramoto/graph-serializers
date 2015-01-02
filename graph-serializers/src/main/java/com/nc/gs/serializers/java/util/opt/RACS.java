package com.nc.gs.serializers.java.util.opt;

import java.util.Collection;
import java.util.List;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

@SuppressWarnings("unchecked")
public final class RACS {

	static Instantiator ctor;
	static GraphSerializer gs;

	public void inflateData0(Context c, Source src, Object o) {
		GraphSerializer gs = RACS.gs;
		Collection<Object> col = (Collection<Object>) o;
		int sz = src.readVarInt();

		for (int i = 0; i < sz; i++) {
			col.add(gs.read(c, src));
		}
	}

	public void inflateData1(Context c, Source src, Object o) {
		GraphSerializer gs = RACS.gs;
		Collection<Object> col = (Collection<Object>) o;

		int sz = src.readVarInt();
		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int lim = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(lim);

			for (int j = 0; j < lim; j++) {
				if ((fl & 1L << j) != 0) {
					col.add(gs.read(c, src));
				} else {
					col.add(null);
				}
			}
		}
	}

	public Object instantiateKSZ(Source src) {
		src.mark();
		Object rv = ctor.allocate(src.readVarInt());
		src.reset();

		return rv;
	}

	public void writeData0(Context c, Sink dst, Object o) {
		GraphSerializer gs = RACS.gs;
		List<Object> col = (List<Object>) o;
		int sz = col.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			for (int i = 0; i < sz; i++) {
				gs.write(c, dst, col.get(i));
			}
		}
	}

	public void writeData1(Context c, Sink dst, Object o) {
		GraphSerializer gs = RACS.gs;
		List<Object> list = (List<Object>) o;
		int sz = list.size();

		dst.writeVarInt(sz);

		if (sz != 0) {
			int loops = sz >>> 6;
			int l = (sz & 63) == 0 ? loops : loops + 1;

			for (int i = 0; i < l; i++) {
				int start = i << 6;
				int max = i != loops ? 64 : sz & 63;
				int end = start + max;
				int pos = dst.reserveMask(max);
				long fl = 0;

				Object v;
				for (int j = start; j < end; j++) {
					if ((v = list.get(j)) != null) {
						fl |= 1L << j;
						gs.write(c, dst, v);
					}
				}

				dst.encodeMask(max, pos, fl);
			}
		}
	}
}