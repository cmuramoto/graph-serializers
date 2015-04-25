package com.nc.gs.tests.compress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Compress;
import com.nc.gs.meta.Hierarchy;

public interface TypesToCheck {

	interface Compressed {

		public class MapK implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Compress Integer, String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapK other = (MapK) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MapKV implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Compress Integer, @Compress String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapKV other = (MapKV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MapV implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<Integer, @Compress String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapV other = (MapV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapK implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Compress @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapK other = (MultiMapK) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapKV implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Compress @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Compress @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapKV other = (MultiMapKV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapV implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Compress @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapV other = (MultiMapV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}
		}

		public class MultiSeq implements ISeq<Object> {

			@Collection(optimize = true, concreteImpl = ArrayList.class, implForReplacement = true)
			List<@Compress @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> l;

			@Override
			public void add(Object k) {
				if (l == null) {
					l = new ArrayList<>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiSeq other = (MultiSeq) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}

		}

		public class Seq implements ISeq<String> {

			@Collection(optimize = true, concreteImpl = ArrayList.class, implForReplacement = true)
			List<@Compress String> l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new ArrayList<>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				Seq other = (Seq) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}

		}

		public class SequenceArray implements ISeq<String> {

			@Collection(optimize = true)
			@Compress
			String[] l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new String[]{ k };
				} else {
					l = Arrays.copyOf(l, l.length + 1);
					l[l.length - 1] = k;
				}
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				SequenceArray other = (SequenceArray) obj;
				if (!Arrays.equals(l, other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + Arrays.hashCode(l);
				return result;
			}

		}

		public class SequenceSet implements ISeq<String> {

			@Collection(optimize = true, concreteImpl = HashSet.class, implForReplacement = true)
			Set<@Compress String> l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new HashSet<>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				SequenceSet other = (SequenceSet) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}
		}

		public class Simple {
			@Compress
			String v;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				Simple other = (Simple) obj;
				if (v == null) {
					if (other.v != null) {
						return false;
					}
				} else if (!v.equals(other.v)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((v == null) ? 0 : v.hashCode());
				return result;
			}

		}

	}

	interface Default {

		public class MapK implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<Integer, String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapK other = (MapK) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MapKV implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<Integer, String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapKV other = (MapKV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MapV implements IMap<Integer, String> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<Integer, String> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MapV other = (MapV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Integer k, String v) {
				if (m == null) {
					m = new HashMap<>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapK implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapK other = (MultiMapK) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapKV implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapKV other = (MultiMapKV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}

		}

		public class MultiMapV implements IMap<Object, Object> {

			@com.nc.gs.meta.Map(optimize = true, concreteImpl = HashMap.class)
			Map<@Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object, @Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> m;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiMapV other = (MultiMapV) obj;
				if (m == null) {
					if (other.m != null) {
						return false;
					}
				} else if (!m.equals(other.m)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((m == null) ? 0 : m.hashCode());
				return result;
			}

			@Override
			public void put(Object k, Object v) {
				if (m == null) {
					m = new HashMap<Object, Object>();
				}
				m.put(k, v);
			}
		}

		public class MultiSeq implements ISeq<Object> {

			@Collection(optimize = true, concreteImpl = ArrayList.class, implForReplacement = true)
			List<@Hierarchy(complete = true, types = { Integer.class, Long.class, String.class }) Object> l;

			@Override
			public void add(Object k) {
				if (l == null) {
					l = new ArrayList<>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				MultiSeq other = (MultiSeq) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}

		}

		public class Seq implements ISeq<String> {

			@Collection(optimize = true, concreteImpl = ArrayList.class, implForReplacement = true)
			List<String> l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new ArrayList<>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				Seq other = (Seq) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}

		}

		public class SequenceArray implements ISeq<String> {

			String[] l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new String[]{ k };
				} else {
					l = Arrays.copyOf(l, l.length + 1);
					l[l.length - 1] = k;
				}
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				SequenceArray other = (SequenceArray) obj;
				if (!Arrays.equals(l, other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + Arrays.hashCode(l);
				return result;
			}

		}

		public class SequenceSet implements ISeq<String> {

			@Collection(optimize = true, concreteImpl = HashSet.class, implForReplacement = true)
			Set<String> l;

			@Override
			public void add(String k) {
				if (l == null) {
					l = new HashSet<String>();
				}
				l.add(k);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				SequenceSet other = (SequenceSet) obj;
				if (l == null) {
					if (other.l != null) {
						return false;
					}
				} else if (!l.equals(other.l)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((l == null) ? 0 : l.hashCode());
				return result;
			}
		}

		public class Simple {
			String v;

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				Simple other = (Simple) obj;
				if (v == null) {
					if (other.v != null) {
						return false;
					}
				} else if (!v.equals(other.v)) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((v == null) ? 0 : v.hashCode());
				return result;
			}
		}

	}

	interface IMap<K, V> {

		void put(K k, V v);

	}

	interface ISeq<K> {

		void add(K k);
	}

}
