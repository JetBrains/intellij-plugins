// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Implementation of {@link Properties} which has a defined order of elements.
 * Properties will be returned in the same order in which they are inserted.
 */
public class OrderedProperties extends Properties {
  private final Set<Object> myKeys = new LinkedHashSet<>();

  public static @NotNull OrderedProperties fromMap(@NotNull Map<String, String> map) {
    OrderedProperties result = new OrderedProperties();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      result.setProperty(entry.getKey(), entry.getValue());
    }
    return result;
  }

  public @NotNull Map<String, String> toMap() {
    Map<String, String> result = new LinkedHashMap<>(size());
    for (String name : stringPropertyNames()) {
      result.put(name, getProperty(name));
    }
    return result;
  }

  @Override
  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(myKeys);
  }

  @Override
  public Enumeration<?> propertyNames() {
    return Collections.enumeration(myKeys);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<String> stringPropertyNames() {
    return (Set)Collections.unmodifiableSet(myKeys);
  }

  @SuppressWarnings("UseOfPropertiesAsHashtable")
  @Override
  public synchronized Object put(Object key, Object value) {
    myKeys.add(key);
    return super.put(key, value);
  }

  @Override
  public synchronized Object remove(Object key) {
    myKeys.remove(key);
    return super.remove(key);
  }

  @Override
  public synchronized void clear() {
    myKeys.clear();
    super.clear();
  }
}
