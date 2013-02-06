package org.jetbrains.plugins.cucumber.java.psi;

import com.intellij.util.containers.HashMap;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GherkinDictionary {

  private static final Set<String> myKeywordsWithNoSpaceAfter = new HashSet<String>();

  static{
    myKeywordsWithNoSpaceAfter.add("Lorsqu'");
  }

  private final GherkinI18nJsonToKeywordTable myConverter;
  private final Map<String, GherkinKeywordTable> myAlreadyLoaded = new HashMap<String, GherkinKeywordTable>();
  private final GherkinLoader myLoader;

  public GherkinDictionary(GherkinI18nJsonToKeywordTable converter, GherkinLoader loader) {
    this.myConverter = converter;
    this.myLoader = loader;
  }

  public GherkinKeywordTable forLanguage(String language) {
    if(unsupported(language)) {
      return new GherkinKeywordTable();
    }
    return getTableFor(language);
  }

  private GherkinKeywordTable getTableFor(String language) {
    GherkinKeywordTable table = myAlreadyLoaded.get(language);
    if (null == table) {
      table = convert(jsonFor(language));
      myAlreadyLoaded.put(language, table);
    }
    return table;
  }

  private boolean unsupported(String language) {
    return !myLoader.supportedLanguages().contains(language);
  }

  public boolean isSpaceAfterKeyword(String language, String keyword) {
    return !myKeywordsWithNoSpaceAfter.contains(keyword);
  }

  private GherkinKeywordTable convert(Map<String, String> rawInputFromI18nJsonFileFor) {
    return myConverter.convert(rawInputFromI18nJsonFileFor);
  }

  private Map<String, String> jsonFor(String isoCode) {
    return myLoader.getRawInputFromI18nJsonFileFor(isoCode);
  }
}