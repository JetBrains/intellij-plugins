package org.jetbrains.plugins.cucumber.java.psi;

import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class GherkinDictionaryTest {

  private final GherkinI18nJsonToKeywordTable converter = new GherkinI18nJsonToKeywordTable();
  private final GherkinLoader loader = new GermanKeywords();

  @Test
  public void french_Lorsqu_isAKeyWordWithoutASpaceBehindIt() throws Exception {
    assertThat(dictionary().isSpaceAfterKeyword("not important", "Lorsqu'"), is(false));
  }

  @Test
  public void returnAnEmptyKeywordTableForAnUnsupportedLanguage() throws Exception {
    GherkinKeywordTable table = dictionary().forLanguage("bogus");
    assertThat(table.getStepKeywords().isEmpty(), is(true));
  }

  @Test
  public void forAvailableLanguagesTransformTheDataFromTheLoaderWithTheConverter() throws Exception {
    assertThat(dictionary().forLanguage("de").getFeatureSectionKeyword(), is("Funktionalität"));
  }

  @Test
  public void convertOnlyOnce() throws Exception {
    GherkinDictionary dictionary = dictionary();
    GherkinKeywordTable first = dictionary.forLanguage("de");
    GherkinKeywordTable second = dictionary.forLanguage("de");
    assertThat(first, sameInstance(second));
  }

  private GherkinDictionary dictionary() {
    return new GherkinDictionary(converter, loader);
  }
}