// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinKeywordTable {
  private final Map<IElementType, Collection<String>> myType2KeywordsTable = new HashMap<>();

  public GherkinKeywordTable() {
    for (IElementType type : GherkinTokenTypes.KEYWORDS.getTypes()) {
      myType2KeywordsTable.put(type, new ArrayList<>());
    }
  }

  public void putAllKeywordsInto(Map<String, IElementType> target) {
    for (IElementType type : this.getTypes()) {
      final Collection<String> keywords = this.getKeywords(type);
      if (keywords != null) {
        for (String keyword : keywords) {
          target.put(keyword, type);
        }
      }
    }
  }

  public void put(IElementType type, String keyword) {
    if (GherkinTokenTypes.KEYWORDS.contains(type)) {
      Collection<String> keywords = getKeywords(type);
      if (keywords == null) {
        keywords = new ArrayList<>(1);
        myType2KeywordsTable.put(type, keywords);
      }
      keywords.add(keyword);
    }
  }

  public Collection<String> getStepKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.STEP_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  public Collection<String> getScenarioKeywords() {
    return getKeywords(GherkinTokenTypes.SCENARIO_KEYWORD);
  }

  public Collection<String> getScenarioLikeKeywords() {
    final Set<String> keywords = new HashSet<>();

    final Collection<String> scenarios = getKeywords(GherkinTokenTypes.SCENARIO_KEYWORD);
    assert scenarios != null;
    keywords.addAll(scenarios);

    final Collection<String> scenarioOutline = getKeywords(GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD);
    assert scenarioOutline != null;
    keywords.addAll(scenarioOutline);

    return keywords;
  }
  
  public @NotNull Collection<String> getRuleKeywords() {
    Collection<String> result = getKeywords(GherkinTokenTypes.RULE_KEYWORD);
    return result == null ? Collections.emptyList() : result;
  }

  public String getScenarioOutlineKeyword() {
    return getScenarioOutlineKeywords().iterator().next();
  }

  public Collection<String> getScenarioOutlineKeywords() {

    final Collection<String> scenarioOutline = getKeywords(GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD);
    assert scenarioOutline != null;

    return scenarioOutline;
  }

  public Collection<String> getBackgroundKeywords() {
    final Collection<String> bg = getKeywords(GherkinTokenTypes.BACKGROUND_KEYWORD);
    assert bg != null;

    return bg;
  }

  public String getExampleSectionKeyword() {
    return getExampleSectionKeywords().iterator().next();
  }

  public Collection<String> getExampleSectionKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.EXAMPLES_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  public String getFeatureSectionKeyword() {
    return getFeaturesSectionKeywords().iterator().next();
  }

  public Collection<String> getFeaturesSectionKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.FEATURE_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  public Collection<IElementType> getTypes() {
    return myType2KeywordsTable.keySet();
  }

  public @Nullable Collection<String> getKeywords(final IElementType type) {
    return myType2KeywordsTable.get(type);
  }

  public boolean tableContainsKeyword(GherkinElementType type, String keyword) {
    Collection<String> alreadyKnownKeywords = getKeywords(type);
    return null != alreadyKnownKeywords && alreadyKnownKeywords.contains(keyword);
  }
}