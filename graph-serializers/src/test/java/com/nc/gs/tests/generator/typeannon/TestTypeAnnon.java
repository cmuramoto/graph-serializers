package com.nc.gs.tests.generator.typeannon;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.FieldInfo.MapMeta;
import com.nc.gs.interpreter.FieldInfo.ShapeVisitor;
import com.nc.gs.interpreter.FieldTrap;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.MapField;
import com.nc.gs.interpreter.MapShape;
import com.nc.gs.tests.generator.typeannon.TheType.Bar;
import com.nc.gs.tests.generator.typeannon.TheType.Baz;
import com.nc.gs.tests.generator.typeannon.TheType.Car;
import com.nc.gs.tests.generator.typeannon.TheType.Foo;
import com.nc.gs.util.Pair;

public class TestTypeAnnon {

	private void checkMapA(FieldInfo info) {
		MapMeta meta = info.mapMeta();

		Assert.assertFalse(meta.getKey().isNullable());

		Assert.assertTrue(meta.getVal().isOnlyPayload());

		Pair<Hierarchy, Hierarchy> p = info.mergeHierarchyForMap();

		Assert.assertSame(Long.class, p.k.superType.runtimeType());

		Assert.assertSame(Car.class, p.v.superType.runtimeType());
	}

	private void checkMapB(FieldInfo info) {
		MapMeta meta = info.mapMeta();
		MapField mf = (MapField) info.asSpecial();
		MapShape shape = mf.shape();

		ShapeVisitor val = meta.getVal();
		Assert.assertTrue(val.isOnlyPayload());
		Assert.assertFalse(val.isNullable());

		Hierarchy h = shape.vs.hierarchy();
		Class<?>[] types = h.runtimeTypes();

		Assert.assertEquals(1, types.length);
		Assert.assertSame(Bar.class, types[0]);

	}

	private void checkMapC(FieldInfo info) {
		MapMeta meta = info.mapMeta();
		MapField mf = (MapField) info.asSpecial();
		MapShape shape = mf.shape();

		ShapeVisitor val = meta.getVal();
		Assert.assertTrue(val.isOnlyPayload());
		Assert.assertFalse(val.isNullable());

		Hierarchy h = shape.vs.hierarchy();
		Class<?>[] types = h.runtimeTypes();

		Assert.assertEquals(3, types.length);
		Assert.assertSame(Bar.class, types[0]);
		Assert.assertSame(Baz.class, types[1]);
		Assert.assertSame(Foo.class, types[2]);
	}

	@Test
	public void run() throws IOException {

		ClassInfo classInfo = ClassInfo.getInfo(TheType.class, FieldTrap.DEFAULT, true);

		LinkedList<FieldInfo> fields = classInfo.fields;

		checkMapA(fields.pop());
		checkMapB(fields.pop());
		checkMapC(fields.pop());

		SerializerFactory.serializer(TheType.class);
	}

}
