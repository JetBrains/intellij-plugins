// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;

public class MapParameterTypeManager implements ParameterTypeManager {
  public static final MapParameterTypeManager DEFAULT = new MapParameterTypeManager(STANDARD_PARAMETER_TYPES);

  private Map<String, String> myParameterTypes;

  public MapParameterTypeManager(Map<String, String> parameterTypes) {
    myParameterTypes = parameterTypes;
  }

  @Nullable
  @Override
  public String getParameterTypeValue(@NotNull String name) {
    return myParameterTypes.get(name);
  }
}
