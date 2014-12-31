package com.nc.gs.tests.generator.ic.mixed.overload;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import symbols.io.abstraction._GraphSerializer;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;

public class ShowCode {

	@Test
	public void run() throws NoSuchMethodException, SecurityException {
		GraphSerializer serializer = SerializerFactory
				.serializer(RedNode.class);

		Method writeRedNodesLeft = serializer.getClass().getDeclaredMethod(
				_GraphSerializer.IC_MULTI_W + "left",
				Context.class, ByteBuffer.class, Node.class);

		Method writeNodesLeft = serializer.getClass().getDeclaredMethod(
				_GraphSerializer.IC_MULTI_W + "left"
						+ "_ov_0", Context.class, ByteBuffer.class, Node.class);

		Assert.assertNotNull(writeNodesLeft);
		Assert.assertNotNull(writeRedNodesLeft);
		Assert.assertNotEquals(writeNodesLeft, writeRedNodesLeft);

	}

}
