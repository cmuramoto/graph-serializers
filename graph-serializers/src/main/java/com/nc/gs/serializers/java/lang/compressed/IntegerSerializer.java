package com.nc.gs.serializers.java.lang.compressed;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class IntegerSerializer extends GraphSerializer {

	@Override
	public Object instantiate(Source src) {
		return Integer.valueOf(src.readInt());
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeInt((Integer) o);
	}

}
