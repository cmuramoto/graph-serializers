package com.nc.gs.serializers.java.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class GregorianCalendarSerializer extends GraphSerializer {

	static TimeZone tz;

	@Override
	public Object instantiate(Source src) {
		long ms = src.readLong();
		byte tag = src.readByte();
		GregorianCalendar gc = tag == 0 ? new GregorianCalendar() : new GregorianCalendar(tz(src.readUTF().intern()));
		gc.setTimeInMillis(ms);

		return gc;
	}

	private TimeZone tz(String id) {
		TimeZone zone = tz;
		if (zone != null && zone.getID().equals(id)) {
			return zone;
		} else {
			tz = zone = TimeZone.getTimeZone(id);
		}
		return zone;
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
