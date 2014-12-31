package com.nc.gs.tests.serializers.reification;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import symbols.io.abstraction._GraphSerializer._R_;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.generator.Reifier;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.serializers.java.util.DateSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestReificationFromUserSerializers extends AbstractRoundTripTests {

	@Test
	public void shouldReifyDateSerializer() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		GraphSerializer ser = Reifier.reify(Date.class, DateSerializer.class);

		// checkId(ser, Date.class);

		// non-reified invocation
		Sink dst = new Sink();

		Date now = new Date();

		try (Context c = Context.writing()) {
			ser.writeData(c, dst, now);
		}

		try (Context c = Context.reading()) {
			Object o = ser.readData(c, dst.mirror());

			Assert.assertEquals(o, now);
		}

		dst.clear();

		Method write = ser.getClass().getDeclaredMethod(_R_.writeData, Context.class, Sink.class, Date.class);

		Method read = ser.getClass().getDeclaredMethod(_R_.readOpaque, Context.class, Source.class);

		try (Context c = Context.writing()) {
			write.invoke(null, c, dst, now);
		}

		dst.clear();

		try (Context c = Context.reading()) {
			Object o = read.invoke(null, c, dst.mirror());

			Assert.assertEquals(o, now);
		}
	}

	@Test
	public void shouldReifyStringSerializer() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		GraphSerializer ser = Reifier.reify(String.class, StringSerializer.class);

		// checkId(ser, String.class);

		// non-reified invocation
		Sink dst = new Sink();

		try (Context c = Context.writing()) {
			ser.writeData(c, dst, "42");
		}

		dst.clear();

		try (Context c = Context.reading()) {
			Object o = ser.readData(c, dst.mirror());

			Assert.assertEquals(o, "42");
		}

		dst.clear();

		Method write = ser.getClass().getDeclaredMethod(_R_.writeData, Context.class, Sink.class, String.class);

		Method read = ser.getClass().getDeclaredMethod(_R_.readOpaque, Context.class, Source.class);

		try (Context c = Context.writing()) {
			write.invoke(null, c, dst, "42");
		}

		dst.clear();

		try (Context c = Context.reading()) {
			Object o = read.invoke(null, c, dst.mirror());

			Assert.assertEquals(o, "42");
		}
	}

}
