package com.nc.gs.tests.serializers.fastbootstrap;

import java.util.concurrent.Callable;

import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.io.Source;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestDeserializeProxy extends AbstractRoundTripTests {

	byte[] dump = { 3, 1, 4, 3, 50, 100, 5, 1, 6, 1, 75, 99, 111, 109, 46, 110, 99, 46, 103, 115, 46, 116, 101, 115, 116, 115, 46, 115, 101, 114, 105, 97, 108, 105, 122, 101, 114, 115, 46, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 114, 101, 102, 108, 101, 99, 116, 46, 68, 117, 109, 112, 101, 114, 36, 78, 97, 109, 101, 100, 73, 110, 118, 111, 99, 97, 116, 105, 111, 110, 72, 97, 110, 100, 108,
			101, 114, 3, 3, 7, 1, 8, 1, 52, 99, 111, 109, 46, 110, 99, 46, 103, 115, 46, 116, 101, 115, 116, 115, 46, 115, 101, 114, 105, 97, 108, 105, 122, 101, 114, 115, 46, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 114, 101, 102, 108, 101, 99, 116, 46, 68, 117, 109, 112, 101, 114 };

	@Test
	public void run() throws Exception {
		Object rec;

		try (Context c = Context.reading(); Source src = new Source()) {
			rec = c.readRefAndData(src.filledWith(dump));
		}

		((Runnable) rec).run();

		((Callable<?>) rec).call();

	}

}
