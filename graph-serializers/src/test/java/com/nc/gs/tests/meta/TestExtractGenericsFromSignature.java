package com.nc.gs.tests.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.FieldInfo.CollectionMeta;
import com.nc.gs.interpreter.FieldInfo.ShapeVisitor;
import com.nc.gs.interpreter.VisitationContext;

public class TestExtractGenericsFromSignature {

	@Test
	public void canExtractGenerics() throws IOException {
		try (VisitationContext vc = VisitationContext.current()) {
			ClassInfo info = ClassInfo.getInfo(Composite.class);

			LinkedList<FieldInfo> fields = info.fields;

			int tested = 0;
			for (FieldInfo fi : fields) {
				ExtendedType[] types;
				switch (fi.name) {
				case "map":
					tested++;
					types = fi.getGenericParameters();

					Assert.assertEquals(2, types.length);

					Assert.assertEquals(Type.getInternalName(A.class), types[0].name);
					Assert.assertEquals(Type.getInternalName(B.class), types[1].name);

					break;
				case "set":
					tested++;

					types = fi.getGenericParameters();

					Assert.assertEquals(1, types.length);

					Assert.assertEquals(Type.getInternalName(C.class), types[0].name);

					break;
				case "list":
					tested++;

					types = fi.getGenericParameters();

					Assert.assertEquals(1, types.length);

					Assert.assertEquals(Type.getInternalName(D.class), types[0].name);

					CollectionMeta meta = fi.collectionMeta();

					Assert.assertNotNull(meta);

					ExtendedType type = meta.getConcreteImpl();

					Assert.assertSame(ArrayList.class, type.runtimeType());

					ShapeVisitor shapeVisitor = meta.getShape();

					Assert.assertNotNull(shapeVisitor);

					Assert.assertTrue(shapeVisitor.isOnlyPayload());

					Assert.assertFalse(shapeVisitor.isNullable());

					break;

				default:
					break;
				}
			}

			Assert.assertEquals(3, tested);

			SerializerFactory.serializer(Composite.class);

		}
	}

}
