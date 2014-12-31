package com.nc.gs.tests.serializers.registered;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.io.Source;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestDeserializeProxy extends AbstractRoundTripTests {

	byte[] dump = { 3, 1, 4, 3, 1, 5, 1, 18, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 82, 117, 110, 110, 97, 98, 108, 101, 1, 6, 1, 29, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 99, 111, 110, 99, 117, 114, 114, 101, 110, 116, 46, 67, 97, 108, 108, 97, 98, 108, 101, 7, 1, 8, 1, 89, 99, 111, 109, 46, 110, 99, 46, 103, 115, 46, 116, 101, 115, 116, 115, 46, 115, 101, 114, 105, 97, 108, 105,
			122, 101, 114, 115, 46, 102, 97, 115, 116, 98, 111, 111, 116, 115, 116, 114, 97, 112, 46, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 114, 101, 102, 108, 101, 99, 116, 46, 68, 117, 109, 112, 101, 114, 36, 78, 97, 109, 101, 100, 73, 110, 118, 111, 99, 97, 116, 105, 111, 110, 72, 97, 110, 100, 108, 101, 114, 3, 3, 9, 1, 10, 1, 66, 99, 111, 109, 46, 110, 99, 46, 103, 115, 46, 116, 101,
			115, 116, 115, 46, 115, 101, 114, 105, 97, 108, 105, 122, 101, 114, 115, 46, 102, 97, 115, 116, 98, 111, 111, 116, 115, 116, 114, 97, 112, 46, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 114, 101, 102, 108, 101, 99, 116, 46, 68, 117, 109, 112, 101, 114 };

	@Test
	public void run() throws Exception {
		Object rec;

		try (Context c = Context.reading(); Source bb = new Source()) {
			rec = c.readRefAndData(bb.filledWith(dump));
		}

		((Runnable) rec).run();

		((Callable<?>) rec).call();

	}

}
