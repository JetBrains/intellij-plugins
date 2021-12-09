// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;


public interface GherkinScenarioOutline extends GherkinStepsHolder {
  @NotNull
  List<GherkinExamplesBlock> getExamplesBlocks();

  @Nullable
  Map<String, String> getOutlineTableMap();
}
