package org.jetbrains.plugins.ruby.motion.bridgesupport;

import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RNameUtilCore;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class Class extends FunctionHolder {
  private String myName;
  private Map<String, Class> mySubClasses;

  public Class(final String name) {
    myName = name;
    mySubClasses = new HashMap<String, Class>();
  }

  public String getName() {
    return myName;
  }

  public Collection<Class> getSubClasses() {
    return mySubClasses.values();
  }

  public Class getSubClass(String name) {
    return mySubClasses.get(name);
  }

  public static Map<String, Class> mergeClasses(Collection<Class> classes) {
    final Map<String, Class> result = new HashMap<String, Class>();
    for (Class clazz : classes) {
      final String name = clazz.getName();
      final int i = name.indexOf(RNameUtilCore.SYMBOL_DELIMITER);
      if (i < 0) {
        result.put(name, clazz);
        continue;
      }

      final String containerName = name.substring(0, i);
      Class container = result.get(containerName);
      if (container == null) {
        container = new Class(containerName);
        result.put(containerName, container);
      }
      clazz.myName = name.substring(i + 2);
      container.mySubClasses.put(clazz.getName(), clazz);
    }
    for (Class container : result.values()) {
      container.mySubClasses = mergeClasses(container.mySubClasses.values());
    }
    return result;
  }
}
