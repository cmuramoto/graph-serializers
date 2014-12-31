package com.nc.gs.generator;

import java.util.Arrays;

import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.Hierarchy;

public final class ICKey {

	static String[] getDescs(ExtendedType[] gens) {
		String[] rv = new String[gens.length];
		for (int i = 0; i < rv.length; i++) {
			rv[i] = gens[i].desc;
		}

		Arrays.sort(rv);

		return rv;
	}

	String fieldDescriptor;
	String[] typeDescriptors;

	boolean onlyPayload;

	public ICKey(FieldInfo info) {
		this(info, info.hierarchy());
	}

	public ICKey(FieldInfo info, ExtendedType[] gens) {
		this(info.desc, getDescs(gens), info.disregardReference());
	}

	public ICKey(FieldInfo info, Hierarchy h) {
		this(info.desc, getDescs(h.types), info.disregardReference());
	}

	public ICKey(String fieldDescriptor, String[] typeDescriptors, boolean onlyPayload) {
		super();
		this.fieldDescriptor = fieldDescriptor;
		Arrays.sort(typeDescriptors);
		this.typeDescriptors = typeDescriptors;
		this.onlyPayload = onlyPayload;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ICKey other = (ICKey) obj;
		if (fieldDescriptor == null) {
			if (other.fieldDescriptor != null)
				return false;
		} else if (!fieldDescriptor.equals(other.fieldDescriptor))
			return false;
		if (onlyPayload != other.onlyPayload)
			return false;
		if (!Arrays.equals(typeDescriptors, other.typeDescriptors))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldDescriptor == null) ? 0 : fieldDescriptor.hashCode());
		result = prime * result + (onlyPayload ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(typeDescriptors);
		return result;
	}

}
