package com.nc.gs.tests.ext;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.nc.gs.core.Serializer;

public interface GExternalizable extends Externalizable {

	@Override
	default void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Serializer.inflateRoot(in, this);
	}

	@Override
	default void writeExternal(ObjectOutput out) throws IOException {
		Serializer.writeRoot(out, this, false);
	}

}
