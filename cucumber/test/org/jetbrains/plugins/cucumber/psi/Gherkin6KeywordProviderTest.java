// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import junit.framework.TestCase;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.util.Collection;

public class Gherkin6KeywordProviderTest extends TestCase {
  GherkinKeywordProvider myKeywordProvider;

  public void testRule() {
    final Collection<String> keywords = myKeywordProvider.getKeywordsTable("en").getRuleKeywords();
    assertTrue(keywords.contains("Rule"));
  }

  public void testExample() {
    final Collection<String> keywords = myKeywordProvider.getKeywordsTable("en").getScenarioKeywords();
    assertTrue(keywords.contains("Example"));
  }

  @Override
  protected void setUp() throws Exception {
    myKeywordProvider = JsonGherkinKeywordProvider.getKeywordProvider(true);
  }

  @Override
  protected void tearDown() throws Exception {
    myKeywordProvider = null;
  }
}
