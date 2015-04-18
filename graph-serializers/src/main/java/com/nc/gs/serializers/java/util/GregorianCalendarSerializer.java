package com.nc.gs.serializers.java.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class GregorianCalendarSerializer extends GraphSerializer {

	static void doTZ(GregorianCalendar gc, Source src, byte tag) {
		if (tag != 0) {
			gc.setTimeZone(tz(src.readUTF().intern()));
		}
	}

	static TimeZone tz(String id) {
		TimeZone zone = tz;
		if (zone != null && zone.getID().equals(id)) {
			return zone;
		} else {
			tz = zone = TimeZone.getTimeZone(id);
		}
		return zone;
	}

	static TimeZone tz;

	@Override
	public Object instantiate(Source src) {
		long ms = src.readLong();
		byte tag = src.readByte();
		GregorianCalendar gc = new GregorianCalendar();
		doTZ(gc, src, tag);
		gc.setTimeInMillis(ms);

		return gc;
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		GregorianCalendar gc = (GregorianCalendar) o;
		dst.writeLong(gc.getTimeInMillis());
		TimeZone zone = gc.getTimeZone();

		if (zone != null) {
			dst.writeByte(1);
			dst.writeUTF(zone.getID());
		} else {
			dst.writeByte(0);
		}
	}

}
