// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;


/// The `Scenario Outline` keyword can be used to run the same Scenario multiple times, with different combinations of values.
///
/// @see <a href="https://cucumber.io/docs/gherkin/reference#scenario-outline">Gherkin Reference | Scenario Outline</a>
public interface GherkinScenarioOutline extends GherkinStepsHolder {
  @NotNull
  List<GherkinExamplesBlock> getExamplesBlocks();

  @Nullable
  Map<String, String> getOutlineTableMap();

  /// @see GherkinStep#getParamsSubstitutions()
  List<String> getParamsSubstitutions();
}
