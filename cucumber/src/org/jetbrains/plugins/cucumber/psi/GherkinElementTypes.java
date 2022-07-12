// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface GherkinElementTypes {
  IElementType FEATURE = new GherkinElementType("feature");
  IElementType FEATURE_HEADER = new GherkinElementType("feature header");
  IElementType SCENARIO = new GherkinElementType("scenario");
  IElementType STEP = new GherkinElementType("step");
  IElementType STEP_PARAMETER = new GherkinElementType("step parameter");
  IElementType SCENARIO_OUTLINE = new GherkinElementType("scenario outline");
  IElementType RULE = new GherkinElementType("rule");
  IElementType EXAMPLES_BLOCK = new GherkinElementType("examples block");
  IElementType TABLE = new GherkinElementType("table");
  IElementType TABLE_HEADER_ROW = new GherkinElementType("table header row");
  IElementType TABLE_ROW = new GherkinElementType("table row");
  IElementType TABLE_CELL = new GherkinElementType("table cell");
  IElementType TAG = new GherkinElementType("tag");
  IElementType PYSTRING = new GherkinElementType("pystring");

  TokenSet SCENARIOS = TokenSet.create(SCENARIO, SCENARIO_OUTLINE);
}
