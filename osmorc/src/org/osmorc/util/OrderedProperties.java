package org.osmorc.util;

import java.util.*;

/**
 * Implementation of {@link Properties} which has a defined order of elements. Properties will be returned in the same order in which they are inserted.
 */
@SuppressWarnings({"unchecked"})
public class OrderedProperties extends Properties {

  private final LinkedHashSet keys = new LinkedHashSet();

  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(keys);
  }


  @Override
  public Set<String> stringPropertyNames() {
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public Enumeration<?> propertyNames() {
      return Collections.enumeration(keys);
  }

  public synchronized Object put(Object key, Object value) {
    keys.add(key);
    //noinspection UseOfPropertiesAsHashtable
    return super.put(key, value);
  }

  @Override
  public synchronized Object remove(Object key) {
    keys.remove(key);
    return super.remove(key);
  }

  @Override
  public synchronized void clear() {
    keys.clear();
    super.clear();
  }
}
