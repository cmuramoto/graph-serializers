package com.nc.gs.serializers.java.lang.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public final class ProxySerializer extends GraphSerializer {

	static InvocationHandler fakeHandler() {
		InvocationHandler ih = fake;

		if (ih == null) {
			ih = fake = (proxy, method, args) -> null;
		}

		return ih;
	}

	static final long H_OFF = Utils.fieldOffset(Proxy.class, "h");

	static InvocationHandler fake;

	final Class<?>[] intfs;

	public ProxySerializer(Class<?> proxy) {
		intfs = proxy.getInterfaces();
	}

	@SuppressWarnings("restriction")
	@Override
	public void inflateData(Context c, Source src, Object o) {
		Utils.U.putObject(o, H_OFF, c.readRefAndData(src));
	}

	@Override
	public Object instantiate(Source src) {
		return Utils.allocateInstance(Context.lookupOrCreate(intfs));
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		c.writeRefAndData(dst, Proxy.getInvocationHandler(o));
	}

}