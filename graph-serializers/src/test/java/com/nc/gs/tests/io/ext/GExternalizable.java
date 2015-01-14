package com.nc.gs.tests.io.ext;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.nc.gs.core.Context;

public interface GExternalizable extends Externalizable {

	@Override
	default void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		try (Context c = Context.reading()) {
			c.inflate(in, this);
		}
	}

	@Override
	default void writeExternal(ObjectOutput out) throws IOException {
		try (Context c = Context.writing()) {
			c.write(out, out, false);
		}
	}

}
