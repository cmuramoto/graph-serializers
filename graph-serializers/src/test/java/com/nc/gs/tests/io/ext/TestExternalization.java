package com.nc.gs.tests.io.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

public class TestExternalization {

	public static Object deserialize(byte[] src) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(src))) {
			return ois.readObject();
		}
	}

	public static byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(o);
		}
		return out.toByteArray();
	}

	@Test
	public void run() throws IOException, ClassNotFoundException {
		Foo root = new Foo();
		root.a = "test";
		Foo leaf = new Foo();
		leaf.a = "cool";
		root.next = leaf;
		leaf.next = leaf;

		Foo rec = (Foo) deserialize(serialize(root));

		Assert.assertNotSame(root, rec);
		Assert.assertEquals(root.a, rec.a);
		Assert.assertEquals(root.next.a, rec.next.a);
		Assert.assertSame(rec.next, rec.next.next);

	}

}
