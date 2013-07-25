/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  @NotNull
  @Attribute("path")
  public String getPath() {
    return myPath;
  }

  public void setPath(String path) {
    myPath = StringUtil.notNullize(path);
  }

  @NotNull
  @Attribute("method")
  public String getMethod() {
    return myMethod;
  }

  public void setMethod(String method) {
    myMethod = StringUtil.notNullize(method);
  }

  @NotNull
  @Attribute("scope")
  public Scope getScope() {
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
      //noinspection ConstantConditions
      return null;
    }
  }
}
