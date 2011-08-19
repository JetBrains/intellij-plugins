package com.intellij.flex.maven;

import java.lang.reflect.Method;
import java.util.Comparator;

class MethodComparator implements Comparator<Method> {
  @Override
  public int compare(Method o1, Method o2) {
    return o1.getName().compareTo(o2.getName());
  }
}
