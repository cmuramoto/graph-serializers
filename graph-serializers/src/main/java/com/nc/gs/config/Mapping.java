package com.nc.gs.config;

import gnu.trove.set.hash.THashSet;

import java.lang.reflect.Method;
import java.util.Set;

import com.nc.gs.util.Utils;

// @XmlType(namespace = "https://nc.com/cfg", name = "mapping")
// @XmlAccessorType(XmlAccessType.FIELD)
public class Mapping implements Comparable<Mapping> {

	// private static final QName NS = new QName("https://nc.com/cfg",
	// "mapping");

	public static String cleanStr(String str) {
		return str == null ? null : str.replaceAll("[\\n\\t]", "").trim();
	}

	// @XmlAttribute(required = false)
	private int autoMapArray;

	// @XmlElement(name = "include")
	// @XmlElementWrapper(name = "includes")
	private Set<String> includes;

	// @XmlAttribute
	private boolean opaque;

	// @XmlTransient
	private Class<?> resolvedType;

	// @XmlElement(required = false)
	private String serializer;

	private String serializerLookup;

	// @XmlElement(required = false)
	private String singletonLookup;

	// @XmlAttribute(required = false)
	private boolean skipReify;

	// @XmlElement(required = true)
	private String type;

	// @XmlAttribute(required = false)
	private int typeId;

	public Mapping() {
		super();
	}

	public Mapping(int typeId, String type, String singletonLookup, String serializer) {
		super();
		this.typeId = typeId;
		this.type = type;
		this.singletonLookup = singletonLookup;
		setSerializer(serializer);
	}

	public void addInclude(String text) {
		Set<String> incs = includes;
		if (incs == null) {
			incs = includes = new THashSet<>(2);
		}
		incs.add(text);
	}

	@Override
	public int compareTo(Mapping o) {
		return Integer.compare(typeId, o.typeId);
	}

	public int getAutoMapArray() {
		return autoMapArray;
	}

	public Set<String> getIncludes() {
		return includes;
	}

	public Class<?> getResolvedType() {
		return resolvedType;
	}

	public String getSerializer() {
		return serializer;
	}

	public String getSerializerLookup() {
		return serializerLookup;
	}

	public String getSingletonLookup() {
		return singletonLookup;
	}

	public String getType() {
		return type;
	}

	public int getTypeId() {
		return typeId;
	}

	public boolean isOpaque() {
		return opaque;
	}

	public boolean isSkipReify() {
		return skipReify;
	}

	Class<?> resolveType(String ns) throws ClassNotFoundException {
		Class<?> rt = resolvedType;

		if (rt == null) {
			String t = type;

			if (t.length() == 1) {
				rt = Utils.primitive(t.charAt(0));
			} else {

				String fqn;

				int ix = t.lastIndexOf('[');
				boolean isArray = ix >= 0;

				if (isArray) {
					String name = t.substring(ix + 1);

					if (name.length() == 1) {
						fqn = t;
					} else {
						if (ns == null || ns.isEmpty()) {
							// fully qualified, correct java syntax
							if (t.lastIndexOf(';') == t.length() - 1) {
								fqn = t;
							} else {
								fqn = String.format("%sL%s;", t.substring(0, ix + 1), t.substring(ix + 1));
							}
						} else {
							if (t.lastIndexOf(';') == t.length() - 1) {
								throw new IllegalMappingException("Array Syntax with explicit namespace must not declared characters L and ;");
							} else {
								fqn = String.format("%sL%s.%s;", t.substring(0, ix + 1), ns, name);
							}
						}
					}

				} else {
					if (ns == null || ns.isEmpty() || t.indexOf('.') > -1) {
						fqn = t;
					} else {
						fqn = String.format("%s.%s", ns, t);
					}
				}

				rt = Class.forName(fqn, false, Thread.currentThread().getContextClassLoader());
			}
		}

		return rt;
	}

	public Object resolveTypeOrSingleton() throws ReflectiveOperationException {
		Class<?> basic = resolvedType;

		if (basic == null) {
			throw new IllegalStateException("Type " + type + " could not be resolved.");
		}

		String sl = singletonLookup;

		if (sl == null || (sl = sl.trim()).isEmpty()) {
			return basic;
		}

		Method method = basic.getDeclaredMethod(sl);
		method.setAccessible(true);

		return method.invoke(null);
	}

	public void setAutoMapArray(int autoMapArray) {
		this.autoMapArray = autoMapArray;
	}

	void setNS(String javaNS) {
		try {
			resolvedType = resolveType(javaNS);
		} catch (ClassNotFoundException e) {
			Utils.rethrow(e);
		}
	}

	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	public void setResolvedType(Class<?> resolvedType) {
		this.resolvedType = resolvedType;
	}

	public String setSerializer(String serializer) {
		this.serializer = serializer;
		return serializer;
	}

	public void setSerializerLookup(String serializerLookup) {
		this.serializerLookup = serializerLookup;
	}

	void setSerializerNS(String ns) {
		if (ns != null && !ns.isEmpty()) {
			String ser = setSerializer(cleanStr(getSerializer()));

			if (ser != null && !ser.isEmpty()) {
				if (ser.indexOf('.') == -1) {
					setSerializer(String.format("%s.%s", ns, getSerializer()));
				}
			}
		}

	}

	public void setSingletonLookup(String singletonLookup) {
		this.singletonLookup = singletonLookup;
	}

	public void setSkipReify(boolean skipReify) {
		this.skipReify = skipReify;
	}

	public void setType(String type) {
		this.type = cleanStr(type);
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	@Override
	public String toString() {
		return String.format("[typeId=%d, skipReify=%s, autoMapArray=%d, opaque=%s, type=%s, singletonLookup=%s, serializer=%s, includes=%s, resolvedType=%s]", typeId, skipReify, autoMapArray, opaque, type, singletonLookup, serializer, includes, resolvedType);
	}

	public void validate(boolean acceptsAutoMap) {
		String type = this.type;

		if (type == null || type.isEmpty()) {
			throw new IllegalMappingException("Element <type> must be informed.");
		}

		int am = autoMapArray;

		if (am > 0) {
			if (!acceptsAutoMap) {
				throw new IllegalMappingException("Array Auto Mapping requires baseId on the context set");
			}

			String sl = singletonLookup;
			if (sl != null && sl.isEmpty()) {
				throw new IllegalMappingException("Cannot declare both 'autoMapArray' and 'singletonLookup'");
			}
		}

		String ser = setSerializer(cleanStr(getSerializer()));

		if (ser != null) {
			if (!Utils.classExists(ser)) {
				throw new IllegalMappingException("Declared serializer " + getSerializer() + " could not be located");
			}
		}
	}
}