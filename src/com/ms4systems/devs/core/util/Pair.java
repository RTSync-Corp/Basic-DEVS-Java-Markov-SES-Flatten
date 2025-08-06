/*
/*
/*   Pair is more  primitive than Entry which is private to Hashtable
 */

package com.ms4systems.devs.core.util;

import java.util.*;
@SuppressWarnings({"rawtypes"})
public class Pair implements PairInterface {
	public Object key, value;

	// Empty Pair constructor
	public Pair() {
		key = "key";
		value = "value";
	}

	// Key and value pairs
	public Pair(Object Key, Object Value) {
		key = Key;
		value = Value;
	}

	public String toString() {
		return "key = " + key.toString() + " ,value = " + value.toString();
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		Class cl = getClass();
		if (!cl.isInstance(o))
			return false;
		Pair p = (Pair) o;
		return key.equals(p.key) && value.equals(p.value);
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}

	// Compares two pairs based on hashcodes; used for sorting
	public int compare(Object m, Object n) { // less than
		Class cl = getClass();
		if (!cl.isInstance(m) || !cl.isInstance(n))
			return 0;
		Pair pm = (Pair) m;
		Pair pn = (Pair) n;
		if (m.equals(n))
			return 0;
		if (pm.key.hashCode() < pn.key.hashCode())
			return -1;
		if (pm.key.hashCode() == pn.key.hashCode()
				&& pm.value.hashCode() <= pn.value.hashCode())
			return -1;
		return 1;
	}
}

@SuppressWarnings("rawtypes")
class PairComparator implements Comparator {

	public PairComparator() {
	}

	public boolean equals(Object o) {
		Class cl = getClass();
		if (cl.isInstance(o))
			return true;
		return false;
	}

	public int compare(Object m, Object n) { // less than
		Class cl = null;
		try {
			cl = Class.forName("com.ms4systems.devs.core.util.Pair");
		} catch (Exception e1) {
			System.out.println(e1);
		}
		if (!cl.isInstance(m) || !cl.isInstance(n))
			return 0;
		Pair pm = (Pair) m;
		Pair pn = (Pair) n;
		if (m.equals(n))
			return 0;
		if (pm.key.hashCode() < pn.key.hashCode())
			return -1;
		if (pm.key.hashCode() == pn.key.hashCode()
				&& pm.value.hashCode() <= pn.value.hashCode())
			return -1;
		return 1;
	}
}
