package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class BooleanSerializer extends GraphSerializer {

	@Override
	public Object instantiate(Source src) {
		return src.readBoolean();
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeBoolean((boolean) o);
	}

}
