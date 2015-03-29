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
import com.nc.gs.tests.generator.typeannon.TheType.Bar;
import com.nc.gs.tests.generator.typeannon.TheType.Car;
import com.nc.gs.tests.generator.typeannon.TheType.Foo;
import com.nc.gs.util.Pair;

public class TestTypeAnnon {

	@Test
	public void run() throws IOException {

		ClassInfo classInfo = ClassInfo.getInfo(TheType.class,
				FieldTrap.DEFAULT, true);

		LinkedList<FieldInfo> fields = classInfo.fields;

		FieldInfo info = fields.pop();

		MapMeta meta = info.mapMeta();

		Assert.assertFalse(meta.getKey().isNullable());

		Assert.assertTrue(meta.getVal().isOnlyPayload());

		Pair<Hierarchy, Hierarchy> p = info.mergeHierarchyForMap();

		Assert.assertSame(Long.class, p.k.superType.runtimeType());

		Assert.assertSame(Car.class, p.v.superType.runtimeType());

		info = fields.pop();
		meta = info.mapMeta();
		
		ShapeVisitor val = meta.getVal();
		Assert.assertTrue(val.isOnlyPayload());
		Assert.assertFalse(val.isNullable());
		
		Class<?>[] array = val.getHierarchy().stream().map(et->et.runtimeType()).toArray(l->new Class<?>[l]);
		
		Assert.assertSame(Bar.class, array[0]);
		
		Assert.assertSame(Foo.class, array[1]);
		
		SerializerFactory.serializer(TheType.class);
	}

}
