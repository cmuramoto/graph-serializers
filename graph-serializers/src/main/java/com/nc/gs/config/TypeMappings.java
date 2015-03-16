package com.nc.gs.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.nc.gs.log.Log;

// @XmlRootElement(namespace = "https://nc.com/cfg", name = "typeMappings")
// @XmlAccessorType(XmlAccessType.FIELD)
public class TypeMappings {

	private static int mappingLoop(int ev, XMLStreamReader reader, List<Mapping> mappings) throws XMLStreamException {
		Mapping curr = new Mapping();
		int ac = reader.getAttributeCount();

		for (int i = 0; i < ac; i++) {
			String av = reader.getAttributeValue(i);

			switch (reader.getAttributeName(i).getLocalPart()) {
			case "type":
				curr.setType(av);
				break;
			case "typeId":
				curr.setTypeId(Integer.valueOf(av));
				break;
			case "skipReify":
				curr.setSkipReify(Boolean.valueOf(av));
				break;
			case "autoMapArray":
				curr.setAutoMapArray(Integer.valueOf(av));
				break;
			case "opaque":
				curr.setOpaque(Boolean.valueOf(av));
				break;
			case "singletonLookup":
				curr.setSingletonLookup(av);
				break;
			case "serializer":
				curr.setSerializer(av);
				break;
			case "serializerLookup":
				curr.setSerializerLookup(av);
				break;
			}
		}

		mapping: while ((ev = reader.next()) != XMLEvent.END_DOCUMENT) {
			switch (ev) {
			case XMLEvent.END_ELEMENT:
				switch (reader.getLocalName()) {
				case "mapping":
				case "mappings":
					break mapping;
				default:
					break;
				}
				break;
			case XMLEvent.START_ELEMENT:
				String field = reader.getLocalName();

				switch (field) {

				case "includes":

					include: while ((ev = reader.next()) != XMLEvent.END_DOCUMENT) {
						switch (ev) {
						case XMLEvent.END_ELEMENT:
							switch (reader.getLocalName()) {
							case "mapping":
							case "mappings":
								break mapping;
							case "includes":
								break include;
							default:
								break;
							}
							break;
						case XMLEvent.START_ELEMENT:
							if (reader.getLocalName().equals("include")) {
								ev = skip(ev, reader);

								curr.addInclude(reader.getText());
							}
						}
					}
				}
				break;
			}
		}

		mappings.add(curr);

		return ev;
	}

	private static int mappingsLoop(int ev, XMLStreamReader reader, List<TypeMappings> tms) throws XMLStreamException {
		int ac = reader.getAttributeCount();

		TypeMappings tm = new TypeMappings();
		List<Mapping> mappings = tm.mappings;

		for (int i = 0; i < ac; i++) {
			String av = reader.getAttributeValue(i);

			switch (reader.getAttributeName(i).getLocalPart()) {
			case "javans":
				tm.javans = av;
				break;
			case "serializersns":
				tm.serializersns = av;
				break;
			case "baseId":
				tm.base = Integer.valueOf(av);
				break;
			default:
				break;
			}
		}

		main: while ((ev = reader.getEventType()) != XMLEvent.END_DOCUMENT) {
			ev = reader.next();
			switch (ev) {
			case XMLEvent.END_ELEMENT:
				if (reader.getLocalName().equals("mappings")) {
					break main;
				}
			case XMLEvent.START_ELEMENT:
				String name = reader.getLocalName();

				switch (name) {
				case "replacements":
					tm.replacements = new HashMap<String, String>(2);
					ev = replLoop(ev, reader, tm);

					if (ev == XMLEvent.END_ELEMENT) {
						if (reader.getLocalName().equals("mappings")) {
							break main;
						}
					}

					if (ev == XMLEvent.END_DOCUMENT) {
						break main;
					}

					break;
				case "mapping":
					ev = mappingLoop(ev, reader, mappings);

					if (ev == XMLEvent.END_ELEMENT) {
						if (reader.getLocalName().equals("mappings")) {
							break main;
						}
					}
					if (ev == XMLEvent.END_DOCUMENT) {
						break main;
					}

					break;

				default:
					continue main;
				}

			}
		}

		if (!tm.mappings.isEmpty() || tm.replacements != null) {
			tms.add(tm);
		}

		return ev;
	}

	public static List<TypeMappings> parse(XMLInputFactory factory, InputStream s) throws XMLStreamException {

		XMLStreamReader reader = factory.createXMLStreamReader(s);

		try {
			int ev;

			List<TypeMappings> tms = new ArrayList<TypeMappings>();

			main: while ((ev = reader.getEventType()) != XMLEvent.END_DOCUMENT) {
				ev = reader.next();
				switch (ev) {
				case XMLEvent.END_DOCUMENT:
					break main;
				case XMLEvent.START_ELEMENT:
					String name = reader.getLocalName();

					switch (name) {
					case "mappings":
						ev = mappingsLoop(ev, reader, tms);
						if (ev == XMLEvent.END_DOCUMENT) {
							break main;
						}
						if (ev == XMLEvent.END_ELEMENT) {
							if (reader.getLocalName().equals("mappings")) {
								continue main;
							}
						}
					}
					break;
				}
			}

			return tms;
		} finally {
			reader.close();
		}
	}

	public static List<TypeMappings> parse(XMLInputFactory factory, URL url) throws XMLStreamException, IOException {
		try (InputStream is = url.openStream()) {
			return parse(factory, is);
		}
	}

	private static int replLoop(int ev, XMLStreamReader reader, TypeMappings tm) throws XMLStreamException {

		main: while ((ev = reader.getEventType()) != XMLEvent.END_DOCUMENT) {
			ev = reader.next();
			switch (ev) {
			case XMLEvent.END_DOCUMENT:
				break;
			case XMLEvent.END_ELEMENT:
				if (reader.getLocalName().equals("replacements")) {
					break main;
				}
				if (reader.getLocalName().equals("replacement")) {
					continue;
				}
			case XMLEvent.START_ELEMENT:
				if (!reader.getLocalName().equals("replacement")) {
					continue main;
				}

				int ac = reader.getAttributeCount();

				String from = null;
				String to = null;
				for (int i = 0; i < ac; i++) {
					String av = reader.getAttributeValue(i);

					switch (reader.getAttributeName(i).getLocalPart()) {
					case "from":
						from = av;
						break;
					case "to":
						to = av;
						break;
					default:
						break;
					}
				}

				if (from == null || to == null) {
					throw new IllegalMappingException("Replacement must declare from/to");
				}

				tm.replacements.put(from, to);
			default:
				break;
			}
		}

	return ev;
	}

	private static int skip(int ev, XMLStreamReader reader) throws XMLStreamException {
		while ((ev = reader.next()) != XMLEvent.CDATA && ev != XMLEvent.CHARACTERS) {
		}
		return ev;
	}

	// @XmlAttribute(required = false, name = "baseId")
	Integer base;

	// @XmlAttribute(required = false, name = "javans")
	private String javans;

	// @XmlElement(name = "mapping")
	// @XmlElementWrapper(name = "mappings")
	private ArrayList<Mapping> mappings = new ArrayList<>();

	private Map<String, String> replacements;

	// @XmlAttribute(required = false, name = "serializersns")
	private String serializersns;

	public Set<Mapping> getMappings(boolean failOnCNFE) {
		ArrayList<Mapping> mappings = this.mappings;
		if (mappings == null || mappings.isEmpty()) {
			// throw new IllegalStateException("Mappings not ready/already consumed for ns " +
			// javans);
			return Collections.emptySet();
		}

		String ns = javans;
		String sns = serializersns;

		if (base != null) {
			int b = base;

			ListIterator<Mapping> itr = mappings.listIterator();

			for (; itr.hasNext();) {
				Mapping mapping = itr.next();
				mapping.setSerializerNS(sns);
				mapping.validate(true);
				Class<?> type;

				try {
					mapping.setNS(ns);
					type = mapping.resolveType(ns);
				} catch (ClassNotFoundException ex) {
					if (failOnCNFE) {
						throw new IllegalMappingException(ex.getMessage());
					} else {
						Log.warn("Error in attempt to load %s on bootstrap.", ex.getMessage());
						b++;
						itr.remove();
						continue;
					}
				}

				mapping.setTypeId(b++);

				int autoMaps = mapping.getAutoMapArray();

				if (autoMaps > 0) {
					for (int i = 0; i < autoMaps; i++) {
						Class<?> ct = Array.newInstance(type, 0).getClass();
						Mapping m = new Mapping(b++, null, null, null);
						m.setResolvedType(ct);
						itr.add(m);
						type = ct;
					}
				}
			}
		} else {
			for (Mapping mapping : mappings) {
				mapping.setSerializerNS(sns);
				mapping.validate(false);
				mapping.setNS(ns);
			}
		}

		TreeSet<Mapping> rv = new TreeSet<>(mappings);

		if (!rv.isEmpty()) {
			Log.info("Created context for namespace: <%s> Id Range:[%04d,%04d]", ns == null ? "<anon>" : ns == null ? "anon" : ns, rv.first().getTypeId(), rv.last().getTypeId());
		}

		this.mappings = null;

		return rv;
	}

	public Map<String, String> getReplacements() {
		return replacements;
	}

	@Override
	public String toString() {
		return "TypeMappings [base=" + base + ", javans=" + javans + ", serializersns=" + serializersns + "]";
	}

}
