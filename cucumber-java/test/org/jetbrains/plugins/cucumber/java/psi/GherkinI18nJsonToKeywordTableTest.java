package org.jetbrains.plugins.cucumber.java.psi;

import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GherkinI18nJsonToKeywordTableTest {
  private final Map<String, String> german = new GermanKeywords().getRawInputFromI18nJsonFileFor("de");
  private final GherkinI18nJsonToKeywordTable converter = new GherkinI18nJsonToKeywordTable();

  @Test
  public void mapKey_feature_to_FEATURE_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getFeaturesSectionKeywords(), is(items("Funktionalität")));
  }

  @Test
  public void mapKey_background_to_BACKGROUND_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getBackgroundKeywords(), is(items("Grundlage")));
  }

  @Test
  public void mapKey_scenario_to_SCENARIO_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getKeywords(GherkinTokenTypes.SCENARIO_KEYWORD), is(items("Szenario")));
  }

  @Test
  public void mapKey_scenario_outline_to_SCENARIO_OUTLINE_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getScenarioOutlineKeywords(), is(items("Szenariogrundriss")));
  }

  @Test
  public void mapKey_examples_to_EXAMPLES_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getExampleSectionKeywords(), is(items("Beispiele")));
  }

  @Test
  public void mapKey_given_when_then_and_but_to_STEP_KEYWORD() throws Exception {
    assertThat(derivedFeatureTable().getStepKeywords(), is(items("*", "Aber", "Angenommen", "Gegeben sei", "Dann", "Wenn", "Und")));
  }

  private GherkinKeywordTable derivedFeatureTable() {
    return converter.convert(german);
  }

  private static Collection<String> items(String... items) {
    return Arrays.asList(items);
  }
}