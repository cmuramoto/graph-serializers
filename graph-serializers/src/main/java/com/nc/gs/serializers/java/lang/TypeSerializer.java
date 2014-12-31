package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class TypeSerializer extends GraphSerializer {

	public TypeSerializer() {
	}

	@Override
	public Object instantiate(Source src) {
		return Context.rawReadType(src);
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		Context.rawWriteType(dst, (Class<?>) o);
	}

}