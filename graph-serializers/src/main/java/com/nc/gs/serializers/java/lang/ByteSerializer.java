package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class ByteSerializer extends GraphSerializer {

	@Override
	public Object instantiate(Source src) {
		return src.readByte();
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeByte((byte) o);
	}

}
