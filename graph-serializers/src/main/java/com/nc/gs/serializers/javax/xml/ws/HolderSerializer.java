package com.nc.gs.serializers.javax.xml.ws;

import javax.xml.ws.Holder;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class HolderSerializer extends GraphSerializer {

	@SuppressWarnings("unchecked")
	@Override
	public void inflateData(Context c, Source src, Object o) {
		((Holder<Object>) o).value = c.readRefAndData(src);
	}

	@Override
	public Object instantiate(Source src) {
		return new Holder<Object>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		c.writeRefAndData(dst, ((Holder<Object>) o).value);
	}

}
