package com.nc.gs.tests.io;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.nc.gs.core.Serializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class IOTests extends AbstractRoundTripTests {

	@Test
	public void fileRoundTrip() throws IOException {
		File file = File.createTempFile("test", ".ser");

		Serializer.writeRoot(file, mkString(), false);

		System.out.println(Serializer.readRoot(file, String.class));
	}

	private String mkString() {
		String s = "pooooooo";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

}
