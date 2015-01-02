package com.nc.gs.serializers.java.util.opt;

import com.nc.gs.core.Context;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class ASMulti {

	private static Object IC_R(Context c, Source src) {
		return null;
	}

	private static void IC_W(Context c, Sink dst, Object v) {

	}

	public void inflateData0(Context c, Source src, Object o) {
		Object[] array = (Object[]) o;

		for (int i = 0; i < array.length; i++) {
			array[i] = IC_R(c, src);
		}
	}

	public void inflateData1(Context c, Source src, Object val) {
		Object[] o = (Object[]) val;

		int sz = o.length;

		if (sz == 0) {
			return;
		}

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;
		int ix = 0;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max && ix < o.length; j++) {
				if ((fl & 1L << j) != 0) {
					o[ix++] = IC_R(c, src);
				}
			}
		}
	}

	public void writeData0(Context c, Sink dst, Object o) {
		Object[] array = (Object[]) o;

		dst.writeVarInt(array.length);

		for (Object v : array) {
			IC_W(c, dst, v);
		}
	}

	public void writeData1(Context c, Sink dst, Object val) {
		Object[] o = (Object[]) val;
		int len = o.length;

		dst.writeVarInt(len);

		int loops = len >>> 6;
				int l = (len & 63) == 0 ? loops : loops + 1;

				int ix = 0;

				Object v;
				for (int i = 0; i < l; i++) {
					int max = i != loops ? 64 : len & 63;
					int pos = dst.reserveMask(max);

					long fl = 0L;
					for (int j = 0; j < max; j++) {
						if ((v = o[ix++]) != null) {
							fl |= 1L << j;
							IC_W(c, dst, v);
						}
					}
					dst.encodeMask(max, pos, fl);
				}

	}
}
