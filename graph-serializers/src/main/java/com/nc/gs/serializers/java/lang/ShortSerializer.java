package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class ShortSerializer extends GraphSerializer {

	@Override
	public Object instantiate(Source src) {
		return Short.valueOf(src.readShort());
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeShort((short) o);
	}

}
