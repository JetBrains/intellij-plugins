// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.mxunit;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class CfmlUnitRunnerParameters implements Cloneable {

  public enum Scope {
    Directory, Component, Method
  }

  private @NotNull String myWebPath = "";
  private @NotNull String myPath = "";
  private @NotNull String myMethod = "";

  private static final Scope DEFAULT_SCOPE = Scope.Component;
  private @NotNull Scope myScope = DEFAULT_SCOPE;

  @Attribute("webpath")
  public String getWebPath() {
    return myWebPath;
  }

  public void setWebPath(String webPath) {
    myWebPath = StringUtil.notNullize(webPath);
  }

  @Attribute("path")
  public @NotNull String getPath() {
    return myPath;
  }

  public void setPath(String path) {
    myPath = StringUtil.notNullize(path);
  }

  @Attribute("method")
  public @NotNull String getMethod() {
    return myMethod;
  }

  public void setMethod(String method) {
    myMethod = StringUtil.notNullize(method);
  }

  @Attribute("scope")
  public @NotNull Scope getScope() {
    return myScope;
  }

  public void setScope(Scope scope) {
    myScope = scope != null ? scope : DEFAULT_SCOPE;
  }

  @Override
  public CfmlUnitRunnerParameters clone() {
    try {
      return (CfmlUnitRunnerParameters)super.clone();
    }
    catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
