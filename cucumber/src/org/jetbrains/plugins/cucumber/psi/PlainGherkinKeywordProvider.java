// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlainGherkinKeywordProvider implements GherkinKeywordProvider {
  public static GherkinKeywordTable DEFAULT_KEYWORD_TABLE = new GherkinKeywordTable();
  public static Map<String, IElementType> DEFAULT_KEYWORDS = new HashMap<>();
  private static final Set<String> ourKeywordsWithNoSpaceAfter = new HashSet<>();

  static {
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.FEATURE_KEYWORD, "Feature");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.BACKGROUND_KEYWORD, "Background");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.SCENARIO_KEYWORD, "Scenario");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.RULE_KEYWORD, "Rule");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.SCENARIO_KEYWORD, "Example");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD, "Scenario Outline");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.EXAMPLES_KEYWORD, "Examples");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.EXAMPLES_KEYWORD, "Scenarios");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "Given");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "When");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "Then");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "And");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "But");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "*");
    DEFAULT_KEYWORD_TABLE.put(GherkinTokenTypes.STEP_KEYWORD, "Lorsqu'");
    ourKeywordsWithNoSpaceAfter.add("Lorsqu'");

    DEFAULT_KEYWORD_TABLE.putAllKeywordsInto(DEFAULT_KEYWORDS);
  }

  @Override
  public Collection<String> getAllKeywords(String language) {
    return DEFAULT_KEYWORDS.keySet();
  }

  @Override
  public IElementType getTokenType(String language, String keyword) {
    return DEFAULT_KEYWORDS.get(keyword);
  }

  @Override
  public String getBaseKeyword(String language, String keyword) {
    return keyword;
  }

  @Override
  public boolean isSpaceRequiredAfterKeyword(String language, String keyword) {
    return !ourKeywordsWithNoSpaceAfter.contains(keyword);
  }

  @Override
  public boolean isStepKeyword(String keyword) {
    return DEFAULT_KEYWORDS.get(keyword) == GherkinTokenTypes.STEP_KEYWORD;
  }

  @Override
  @NotNull
  public GherkinKeywordTable getKeywordsTable(@Nullable final String language) {
    return DEFAULT_KEYWORD_TABLE;
  }
}
