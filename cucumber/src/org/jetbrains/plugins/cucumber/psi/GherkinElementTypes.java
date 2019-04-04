package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author yole
 */
public interface GherkinElementTypes {
  IFileElementType GHERKIN_FILE = new IFileElementType(GherkinLanguage.INSTANCE);

  IElementType FEATURE = new GherkinElementType("feature");
  IElementType FEATURE_HEADER = new GherkinElementType("feature header");
  IElementType SCENARIO = new GherkinElementType("scenario");
  IElementType STEP = new GherkinElementType("step");
  IElementType STEP_PARAMETER = new GherkinElementType("step parameter");
  IElementType SCENARIO_OUTLINE = new GherkinElementType("scenario outline");
  IElementType EXAMPLES_BLOCK = new GherkinElementType("examples block");
  IElementType TABLE = new GherkinElementType("table");
  IElementType TABLE_HEADER_ROW = new GherkinElementType("table header row");
  IElementType TABLE_ROW = new GherkinElementType("table row");
  IElementType TABLE_CELL = new GherkinElementType("table cell");
  IElementType TAG = new GherkinElementType("tag");
  IElementType PYSTRING = new GherkinElementType("pystring");

  TokenSet SCENARIOS = TokenSet.create(SCENARIO, SCENARIO_OUTLINE);
}
