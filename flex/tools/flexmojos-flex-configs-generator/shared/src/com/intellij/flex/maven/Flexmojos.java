package com.intellij.flex.maven;

import org.apache.maven.plugin.Mojo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Flexmojos {
  static String getClassifier(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (String)invokePublicMethod(mojo, "getClassifier");
  }

  static String getOutput(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return (String)invokePublicMethod(mojo, "getOutput");
  }

  static File getSourceFileForSwf(Mojo mojo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = mojo.getClass().getDeclaredMethod("getSourceFile");
    method.setAccessible(true);
    return (File)method.invoke(mojo);
  }

  static File getLinkReport(Mojo mojo) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    String linkReport = (String)invokePublicMethod(mojo, "getLinkReport");
    return linkReport == null ? null : new File(linkReport);
  }

  static Object invokePublicMethod(Mojo mojo, String methodName)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = mojo.getClass().getMethod(methodName);
    method.setAccessible(true);
    return method.invoke(mojo);
  }
}
