package org.jetbrains.plugins.cucumber.i18n;

import junit.framework.TestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGherkinKeywordProviderTest extends TestCase {
  private GherkinKeywordProvider myKeywordProvider;

  protected abstract GherkinKeywordProvider buildKeywordProvider() throws IOException;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myKeywordProvider = buildKeywordProvider();
  }

  public void testKeywords() {
    final Collection<String> keywords = myKeywordProvider.getAllKeywords("en");
    assertTrue(keywords.contains("Feature"));
  }

  public void testKeywordsTable_Feature() {
    assertTrue(getKeywordsTableEn().getFeaturesSectionKeywords().contains("Feature"));
  }

  public void testKeywordsTable_ScenarioLike() {
    final Collection<String> keywords = getKeywordsTableEn().getScenarioLikeKeywords();
    assertTrue(keywords.contains("Scenario"));
    assertTrue(keywords.contains("Scenario Outline"));
  }

  public void testKeywordsTable_Examples() {
    assertTrue(getKeywordsTableEn().getExampleSectionKeywords().contains("Examples"));
  }

  public void testKeywordsTable_ExamplesRu() {
    // Beware that translation "Значения" is not official and was added as an exception (RUBY-29359). It needs to stay.
    assertTrue(getKeywordsTableRu().getExampleSectionKeywords().containsAll(List.of("Примеры", "Значения")));
  }

  public void testKeywordsTable_BackGround() {
    assertTrue(getKeywordsTableEn().getBackgroundKeywords().contains("Background"));
  }

  public void testKeywordsTable_Feature_Fi() {
    assertTrue(getKeywordsTableFi().getFeaturesSectionKeywords().contains("Ominaisuus"));
  }

  public void testKeywordsTable_ScenarioLike_Fi() {
    final Collection<String> keywords = getKeywordsTableFi().getScenarioLikeKeywords();
    assertTrue(keywords.contains("Tapaus"));
    assertTrue(keywords.contains("Tapausaihio"));
  }

  public void testKeywordsTable_Examples_Fi() {
    assertTrue(getKeywordsTableFi().getExampleSectionKeywords().contains("Tapaukset"));
  }

  public void testKeywordsTable_BackGround_Fi() {
    assertTrue(getKeywordsTableFi().getBackgroundKeywords().contains("Tausta"));
  }

  public void testCompositeKeywords() {
    final Collection<String> keywords = myKeywordProvider.getAllKeywords("en");
    assertTrue(keywords.contains("Scenarios"));
  }

  public void testLanguage() {
    assertTrue(myKeywordProvider.getAllKeywords("fi").contains("Tapausaihio"));
    assertEquals(GherkinTokenTypes.FEATURE_KEYWORD, myKeywordProvider.getTokenType("fi", "Ominaisuus"));
  }

  public void testSpaceAfterKeyword() {
    assertTrue(myKeywordProvider.isSpaceRequiredAfterKeyword("en", "Given"));
    assertFalse(myKeywordProvider.isSpaceRequiredAfterKeyword("fr", "Lorsqu'"));
  }

  public void testNonKeywords() {
    final Collection<String> keywords = myKeywordProvider.getAllKeywords("en");
    assertFalse(keywords.contains("English"));
    assertFalse(keywords.contains("UTF-8"));
  }

  public void testTokenType() {
    assertEquals(GherkinTokenTypes.STEP_KEYWORD, myKeywordProvider.getTokenType("en", "And"));
    assertEquals(GherkinTokenTypes.STEP_KEYWORD, myKeywordProvider.getTokenType("en", "*"));
  }

  public void testStepKeyword() {
    assertTrue(myKeywordProvider.isStepKeyword("Given"));
    assertTrue(myKeywordProvider.isStepKeyword("Oletetaan"));
    assertFalse(myKeywordProvider.isStepKeyword("Feature"));
  }

  private GherkinKeywordTable getKeywordsTableEn() {
    return myKeywordProvider.getKeywordsTable("en");
  }

  private GherkinKeywordTable getKeywordsTableFi() {
    return myKeywordProvider.getKeywordsTable("fi");
  }

  private GherkinKeywordTable getKeywordsTableRu() {
    return myKeywordProvider.getKeywordsTable("ru");
  }
}
