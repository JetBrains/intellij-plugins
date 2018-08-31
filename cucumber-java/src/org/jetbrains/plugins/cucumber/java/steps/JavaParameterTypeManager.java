// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;

import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;

public class JavaParameterTypeManager implements ParameterTypeManager {
  public static final JavaParameterTypeManager DEFAULT = new JavaParameterTypeManager(STANDARD_PARAMETER_TYPES);

  private Map<String, String> myParameterTypes;

  public JavaParameterTypeManager(Map<String, String> parameterTypes) {
    myParameterTypes = parameterTypes;
  }

  @Nullable
  @Override
  public String getParameterTypeValue(@NotNull String name) {
    return myParameterTypes.get(name);
  }
}
