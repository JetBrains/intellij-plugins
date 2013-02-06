package org.jetbrains.plugins.cucumber.java.psi;

import com.intellij.util.containers.HashSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GherkinJsonLoaderTest {
  private final GherkinJsonLoader loader = new GherkinJsonLoader("/org/jetbrains/plugins/cucumber/java/psi/gherkin-language-sample.json");

  @Test
  public void checkThatTheActualGherkinI18nJsonGetsLoaded() throws Exception {
    GherkinLoader loader = GherkinJsonLoader.loadLanguagesFromGherkinLibrary();
    assertThat(loader.supportedLanguages().size(), is(46));
  }

  @Test
  public void callLoadTheReadTheData() throws Exception {
    assertThat(loader.supportedLanguages().isEmpty(), is(true));
  }

  @Test
  public void provideAllTheLanguagesIsoStringContainedInTheFile() throws Exception {
    loader.load();
    assertThat(loader.supportedLanguages(), is(set("en", "ar")));
  }

  @Test
  public void returnAnEmptyMapForUnknownLanguages() throws Exception {
    assertThat(loadedSampleJson().getRawInputFromI18nJsonFileFor("bogus").isEmpty(), is(true));
  }

  @Test
  public void readName() throws Exception {
    assertThat(rawInputForEnglish().get("name"), is("English"));
  }

  @Test
  public void readNative() throws Exception {
    assertThat(rawInputForEnglish().get("native"), is("English"));
  }

  @Test
  public void readFeature() throws Exception {
    assertThat(rawInputForEnglish().get("feature"), is("Feature|Business Need|Ability"));
  }

  @Test
  public void readBackground() throws Exception {
    assertThat(rawInputForEnglish().get("background"), is("Background"));
  }

  @Test
  public void readScenario() throws Exception {
    assertThat(rawInputForEnglish().get("scenario"), is("Scenario"));
  }

  @Test
  public void readScenarioOutline() throws Exception {
    assertThat(rawInputForEnglish().get("scenario_outline"), is("Scenario Outline|Scenario Template"));
  }

  @Test
  public void readExamples() throws Exception {
    assertThat(rawInputForEnglish().get("examples"), is("Examples|Scenarios"));
  }

  @Test
  public void readGiven() throws Exception {
    assertThat(rawInputForEnglish().get("given"), is("*|Given"));
  }

  @Test
  public void readWhen() throws Exception {
    assertThat(rawInputForEnglish().get("when"), is("*|When"));
  }

  @Test
  public void readThen() throws Exception {
    assertThat(rawInputForEnglish().get("then"), is("*|Then"));
  }

  @Test
  public void readAnd() throws Exception {
    assertThat(rawInputForEnglish().get("and"), is("*|And"));
  }

  @Test
  public void readBut() throws Exception {
    assertThat(rawInputForEnglish().get("but"), is("*|But"));
  }

  private Map<String, String> rawInputForEnglish() {
    return loadedSampleJson().getRawInputFromI18nJsonFileFor("en");
  }

  private GherkinLoader loadedSampleJson() {
    loader.load();
    return loader;
  }

  private static Set<String> set(String... languages) {
    HashSet<String> result = new HashSet<String>();
    result.addAll(Arrays.asList(languages));
    return result;
  }
}