package com.nc.gs.serializers.java.lang;

import java.lang.reflect.Array;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class LeafTypeArraySerializer extends GraphSerializer {

	public static void readBitMask(Context c, Source src, Object[] o, GraphSerializer gs, boolean op) {
		int sz = o.length;
		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		int ix = 0;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max
					// bounds check is not really necessary,
					// however it can help the compiler
					&& ix < sz; j++, ix++) {
				if ((fl & 1L << j) != 0) {
					o[ix] = op ? gs.readData(c, src) : gs.read(c, src);
				}
			}
		}
	}

	public static void writeBitMask(Context c, Sink dst, Object[] o, GraphSerializer gs, boolean op) {

		int len = o.length;

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
							if (op) {
								gs.writeData(c, dst, v);
							} else {
								gs.write(c, dst, v);
							}
						}
					}

					dst.encodeMask(max, pos, fl);
				}
	}

	final GraphSerializer ser;
	final Class<?> type;

	final boolean n;

	final boolean op;

	public LeafTypeArraySerializer(Class<?> type, boolean n, boolean op) {
		this.type = type;
		this.n = n;
		this.op = op;
		ser = SerializerFactory.serializer(type);
	}

	@Override
	public void inflateData(Context c, Source src, Object o) {
		Object[] array = (Object[]) o;
		boolean nullable = n;
		boolean op = this.op;
		GraphSerializer gs = ser;

		if (nullable) {
			readBitMask(c, src, array, gs, op);
		} else {
			for (int i = 0; i < array.length; i++) {
				array[i] = op ? gs.readData(c, src) : gs.read(c, src);
			}
		}
	}

	@Override
	public Object instantiate(Source src) {
		return Array.newInstance(type, src.readVarInt());
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		Object[] array = (Object[]) o;
		boolean nullable = n;
		boolean op = this.op;
		GraphSerializer gs = ser;

		dst.writeVarInt(array.length);

		if (nullable) {
			writeBitMask(c, dst, array, gs, op);
		} else {
			for (Object v : array) {
				if (op) {
					gs.writeData(c, dst, v);
				} else {
					gs.write(c, dst, v);
				}
			}
		}
	}
}