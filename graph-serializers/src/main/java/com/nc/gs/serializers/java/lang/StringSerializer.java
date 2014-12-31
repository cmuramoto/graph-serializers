package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class StringSerializer extends GraphSerializer {

	public StringSerializer() {
	}

	@Override
	public Object instantiate(Source src) {
		return src.readUTF();
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		// The guard here is a hack to prevent VM crash in U.get(null,...).
		// This may happen if a field is marked with @NotNull and the contract is not honored!
		dst.writeUTF((String) o);
	}
}