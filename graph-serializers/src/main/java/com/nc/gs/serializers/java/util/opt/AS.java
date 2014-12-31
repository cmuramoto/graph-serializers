package com.nc.gs.serializers.java.util.opt;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class AS {

	static GraphSerializer gs;

	public void inflateData0(Context c, Source src, Object o) {
		GraphSerializer gs = AS.gs;

		Object[] array = (Object[]) o;

		for (int i = 0; i < array.length; i++) {
			array[i] = gs.read(c, src);
		}
	}

	public void inflateData1(Context c, Source src, Object val) {
		GraphSerializer gs = AS.gs;

		Object[] o = (Object[]) val;
		int sz = o.length;
		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		int ix = 0;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max && ix < sz; j++) {
				if ((fl & 1L << j) != 0) {
					o[ix++] = gs.read(c, src);
				}
			}
		}
	}

	public void writeData0(Context c, Sink dst, Object o) {
		GraphSerializer gs = AS.gs;

		Object[] array = (Object[]) o;

		dst.writeIntP(array.length);

		for (Object v : array) {
			gs.write(c, dst, v);
		}
	}

	public void writeData1(Context c, Sink dst, Object val) {
		GraphSerializer gs = AS.gs;

		Object[] o = (Object[]) val;
		int len = o.length;

		dst.writeInt(len);

		int loops = len >>> 6;
				int l = (len & 63) == 0 ? loops : loops + 1;

				dst.writeIntP(loops);

				int ix = 0;
				Object v;

				for (int i = 0; i < l; i++) {
					int max = i != loops ? 64 : len & 63;
					long fl = 0L;
					int pos = dst.reserveMask(max);

					for (int j = 0; j < max; j++) {
						if ((v = o[ix++]) != null) {
							fl |= 1L << j;
							gs.write(c, dst, v);
						}
					}

					dst.encodeMask(max, pos, fl);
				}
	}
}