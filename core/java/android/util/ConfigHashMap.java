package android.util;

import java.util.HashMap;

public class ConfigHashMap<K,V> extends HashMap<K,V> {

	private static final long serialVersionUID = 10L;

	public V getNonNull(Object o, V def){
		V value = this.get(o);
		
		if(value == null){
			return def;
		}
		return value;
	}
}
