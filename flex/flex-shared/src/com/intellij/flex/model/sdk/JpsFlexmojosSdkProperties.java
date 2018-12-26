package com.intellij.flex.model.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class JpsFlexmojosSdkProperties {

  private final Collection<String> myFlexCompilerClasspath = new ArrayList<>();
  private String myAdlPath = "";
  private String myAirRuntimePath = "";

  public JpsFlexmojosSdkProperties(final Collection<String> flexCompilerClasspath, final String adlPath, final String airRuntimePath) {
    myFlexCompilerClasspath.addAll(flexCompilerClasspath);

    myAdlPath = adlPath;
    myAirRuntimePath = airRuntimePath;
  }

  public Collection<String> getFlexCompilerClasspath() {
    return Collections.unmodifiableCollection(myFlexCompilerClasspath);
  }

  public String getAdlPath() {
    return myAdlPath;
  }

  public String getAirRuntimePath() {
    return myAirRuntimePath;
  }
}
