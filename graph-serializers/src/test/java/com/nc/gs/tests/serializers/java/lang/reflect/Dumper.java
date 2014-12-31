package com.nc.gs.tests.serializers.java.lang.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.log.Log;
import com.nc.gs.tests.AbstractRoundTripTests;

public class Dumper extends AbstractRoundTripTests {

	class NamedInvocationHandler implements InvocationHandler {

		Object ref;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Log.info("%s called", method.getName());

			return null;
		}

	}

	@Test
	public void dump() throws Exception {
		Sink buffer = new Sink();

		NamedInvocationHandler h = new NamedInvocationHandler();

		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ Runnable.class, Callable.class }, h);
		h.ref = proxy;

		((Runnable) proxy).run();

		((Callable<?>) proxy).call();

		try (Context c = Context.writing()) {
			c.writeRefAndData(buffer, proxy);
		}

		Object rec;

		dump(buffer);

		buffer.clear();

		try (Context c = Context.reading()) {
			rec = c.readRefAndData(buffer.mirror());
		}

		((Runnable) rec).run();

		((Callable<?>) rec).call();

	}

	private void dump(Sink buffer) {
		StringBuilder builder = new StringBuilder("byte[] dump= {");

		int lim = buffer.position();

		buffer.clear();

		Source src = buffer.mirror();

		for (int i = 0; i < lim; i++) {
			builder.append(src.read());
			if (i < lim - 1) {
				builder.append(',');
			}
		}

		builder.append("};");

		Log.info("\n\n%s\n\n", builder.toString());
	}

}
