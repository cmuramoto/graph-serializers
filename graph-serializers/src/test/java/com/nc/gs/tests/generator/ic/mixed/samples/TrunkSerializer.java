package com.nc.gs.tests.generator.ic.mixed.samples;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public class TrunkSerializer extends GraphSerializer {

	@Override
	public void inflateData(Context c, Source src, Object o) {

	}

	@Override
	public Object instantiate(Source src) {
		return null;
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {

	}

}
