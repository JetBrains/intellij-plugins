package com.intellij.flex.maven;

import org.apache.maven.plugin.Mojo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class Flexmojos {
  static String getClassifier(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (String)invokePublicMethod(mojo, "getClassifier");
  }

  static String getOutput(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (String)invokePublicMethod(mojo, "getOutput");
  }

  static File getSourceFileForSwf(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = mojo.getClass().getDeclaredMethod("getSourceFile");
    method.setAccessible(true);
    return (File)method.invoke(mojo);
  }

  static Object invokePublicMethod(Mojo mojo, String methodName)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = mojo.getClass().getMethod(methodName);
    method.setAccessible(true);
    return method.invoke(mojo);
  }

  //static Object invokeField(Mojo mojo, String fieldName)
  //  throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
  //  Field field = mojo.getClass().getDeclaredField(fieldName);
  //  field.setAccessible(true);
  //  return field.get(mojo);
  //}
}
