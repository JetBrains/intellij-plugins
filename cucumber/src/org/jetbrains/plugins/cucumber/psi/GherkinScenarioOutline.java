package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public interface GherkinScenarioOutline extends GherkinStepsHolder {
  @NotNull
  List<GherkinExamplesBlock> getExamplesBlocks();

  @Nullable
  Map<String, String> getOutlineTableMap();
}
