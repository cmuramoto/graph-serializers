package com.nc.gs.serializers.java.util;

import static com.nc.gs.util.Utils.U;
import static symbols.io.abstraction._Tags.Serializer.FALSE;
import static symbols.io.abstraction._Tags.Serializer.TRUE;

import java.util.EnumSet;

import symbols.java.util._EnumSet;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class EnumSetSerializer extends GraphSerializer {

	public static EnumSetSerializer basic() {
		EnumSetSerializer rv = basic;

		if (rv == basic) {
			basic = rv = new EnumSetSerializer();
		}

		return rv;
	}

	static final Class<?> REG = Utils.forName(_EnumSet.regularBN);

	static final Class<?> JUMBO = Utils.forName(_EnumSet.jumboBN);

	static final long JE_OFF = Utils.fieldOffset(JUMBO, "elements");

	static final long RE_OFF = Utils.fieldOffset(REG, "elements");

	static final long ET_OFF = Utils.fieldOffset(_EnumSet.BN, "elementType");

	static EnumSetSerializer basic;

	private EnumSetSerializer() {
	}

	@SuppressWarnings("restriction")
	@Override
	public Object instantiate(Source src) {
		Class<?> type = Context.rawReadType(src);

		Object rv;

		if (src.readByte() == TRUE) {
			rv = Utils.allocateInstance(JUMBO);
			long[] l = src.readLongArray();
			U.putObject(rv, JE_OFF, l);
		} else {
			rv = Utils.allocateInstance(REG);
			U.putLong(rv, RE_OFF, src.readLongP());
		}

		U.putObject(rv, ET_OFF, type);

		return rv;
	}

	@SuppressWarnings({ "restriction", "rawtypes" })
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		EnumSet es = (EnumSet) o;

		Class<?> type = (Class<?>) U.getObject(es, ET_OFF);

		Context.rawWriteType(dst, type);

		if (es.getClass() == REG) {
			dst.writeByte(FALSE);
			dst.writeLongP(U.getLong(es, RE_OFF));
		} else {
			dst.writeByte(TRUE);
			long[] l = (long[]) U.getObject(es, JE_OFF);
			dst.writeIntP(l.length);
			dst.write(l, 0, l.length);
		}
	}

}
