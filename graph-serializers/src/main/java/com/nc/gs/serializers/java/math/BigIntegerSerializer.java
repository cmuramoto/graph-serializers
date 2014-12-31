package com.nc.gs.serializers.java.math;

import static com.nc.gs.util.Utils.U;

import java.math.BigInteger;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class BigIntegerSerializer extends GraphSerializer {

	static final long SIGNUM = Utils.fieldOffset(BigInteger.class, "signum");

	static final long MAGNITUDE = Utils.fieldOffset(BigInteger.class, "mag");

	public BigIntegerSerializer() {
	}

	@SuppressWarnings("restriction")
	@Override
	public void inflateData(Context c, Source src, Object o) {
		U.putIntVolatile(o, SIGNUM, src.readByte());
		int[] mag = src.readIntArray();
		U.putObjectVolatile(o, MAGNITUDE, mag);
	}

	@Override
	public Object instantiate(Source src) {
		return Utils.allocateInstance(BigInteger.class);
	}

	@SuppressWarnings("restriction")
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeByte((byte) U.getInt(o, SIGNUM));
		int[] mag = (int[]) U.getObject(o, MAGNITUDE);
		dst.putIntArray(mag);
	}
}