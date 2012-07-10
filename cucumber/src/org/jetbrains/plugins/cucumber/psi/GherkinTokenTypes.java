package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.TokenSet;

/**
 * @author yole
 */
public interface GherkinTokenTypes {
  GherkinElementType COMMENT = new GherkinElementType("COMMENT");
  GherkinElementType TEXT = new GherkinElementType("TEXT");
  GherkinElementType EXAMPLES_KEYWORD = new GherkinElementType("EXAMPLES_KEYWORD");
  GherkinElementType FEATURE_KEYWORD = new GherkinElementType("FEATURE_KEYWORD");
  GherkinElementType BACKGROUND_KEYWORD = new GherkinElementType("BACKGROUND_KEYWORD");
  GherkinElementType SCENARIO_KEYWORD = new GherkinElementType("SCENARIO_KEYWORD");
  GherkinElementType SCENARIO_OUTLINE_KEYWORD = new GherkinElementType("SCENARIO_OUTLINE_KEYWORD");
  GherkinElementType STEP_KEYWORD = new GherkinElementType("STEP_KEYWORD");
  GherkinElementType STEP_PARAMETER_BRACE = new GherkinElementType("STEP_PARAMETER_BRACE");
  GherkinElementType STEP_PARAMETER_TEXT = new GherkinElementType("STEP_PARAMETER_TEXT");
  GherkinElementType COLON = new GherkinElementType("COLON");
  GherkinElementType TAG = new GherkinElementType("TAG");
  GherkinElementType PYSTRING = new GherkinElementType("PYSTRING_QUOTES");
  GherkinElementType PYSTRING_TEXT = new GherkinElementType("PYSTRING_TEXT");
  GherkinElementType PIPE = new GherkinElementType("PIPE");
  GherkinElementType TABLE_CELL = new GherkinElementType("TABLE_CELL");

  TokenSet KEYWORDS = TokenSet.create(FEATURE_KEYWORD,
                                      BACKGROUND_KEYWORD, SCENARIO_KEYWORD, SCENARIO_OUTLINE_KEYWORD,
                                      EXAMPLES_KEYWORD, EXAMPLES_KEYWORD,
                                      STEP_KEYWORD);
}
