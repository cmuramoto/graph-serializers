package com.nc.gs.serializers.java.util;

import static com.nc.gs.util.Utils.U;

import java.util.EnumSet;

import symbols.java.util._EnumSet;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class EnumSetSerializer extends GraphSerializer {

	public static EnumSetSerializer of(Class<?> kind) {
		EnumSetSerializer rv;
		if (kind == null || !kind.isEnum()) {
			rv = basic;

			if (rv == basic) {
				basic = rv = new EnumSetSerializer();
			}
		} else {
			rv = new EnumSetSerializer(kind);
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
		this(null);
	}

	private EnumSetSerializer(@SuppressWarnings("rawtypes") Class type) {
		this.type = type;
	}

	final Class<?> type;

	@SuppressWarnings({ "restriction", "unchecked", "rawtypes" })
	@Override
	public Object instantiate(Source src) {
		Class type = this.type == null ? Context.rawReadType(src) : this.type;
		EnumSet es = EnumSet.noneOf(type);

		if (es.getClass() == REG) {
			U.putLong(es, RE_OFF, src.readVarLong());
		} else {
			long[] l = src.readLongArray();
			U.putObject(es, JE_OFF, l);
		}

		return es;
	}

	@SuppressWarnings({ "restriction" })
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		Class<?> type = this.type;
		if (type == null) {
			Context.rawWriteType(dst, (Class<?>) U.getObject(o, ET_OFF));
		}

		if (o.getClass() == REG) {
			dst.writeVarLong(U.getLong(o, RE_OFF));
		} else {
			long[] l = (long[]) U.getObject(o, JE_OFF);
			dst.writeVarInt(l.length);
			dst.write(l, 0, l.length);
		}
	}

}
