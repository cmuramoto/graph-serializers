package com.nc.gs.serializers.java.util;

import java.util.Date;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class DateSerializer extends GraphSerializer {

	public DateSerializer() {
	}

	@Override
	public Object instantiate(Source src) {
		return new Date(src.readLong());
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeLong(((Date) o).getTime());
	}

}
