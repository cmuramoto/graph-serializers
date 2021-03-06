package com.nc.gs.serializers.java.lang.compressed;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class FloatSerializer extends GraphSerializer {

	@Override
	public Object instantiate(Source src) {
		return Float.valueOf(src.readVarFloat());
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeVarFloat((float) o);
	}

}
