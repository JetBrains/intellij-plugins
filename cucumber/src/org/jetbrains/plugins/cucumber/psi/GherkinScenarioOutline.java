package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author yole
 */
public interface GherkinScenarioOutline extends GherkinStepsHolder {
  @NotNull
  List<GherkinExamplesBlock> getExamplesBlocks();
}
