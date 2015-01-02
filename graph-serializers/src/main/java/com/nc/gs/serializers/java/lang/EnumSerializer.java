package com.nc.gs.serializers.java.lang;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class EnumSerializer extends GraphSerializer {

	final Object[] values;

	@SuppressWarnings("unchecked")
	public EnumSerializer(@SuppressWarnings("rawtypes") Class type) {
		values = Utils.getSharedEnumConstants(type);
	}

	@Override
	public Object instantiate(Source src) {
		return values[src.readVarInt()];
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		dst.writeVarInt(((Enum<?>) o).ordinal());
	}

}