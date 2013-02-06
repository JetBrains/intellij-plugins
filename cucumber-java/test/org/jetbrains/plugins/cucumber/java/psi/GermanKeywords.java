package org.jetbrains.plugins.cucumber.java.psi;

import com.intellij.util.containers.HashMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GermanKeywords implements GherkinLoader {
  private final Map<String, String> keywordsAsParsedFromJson = new HashMap<String, String>();

  GermanKeywords() {
    keywordsAsParsedFromJson.put("name", "German");
    keywordsAsParsedFromJson.put("native", "Deutsch");
    keywordsAsParsedFromJson.put("feature", "Funktionalität");
    keywordsAsParsedFromJson.put("background", "Grundlage");
    keywordsAsParsedFromJson.put("scenario", "Szenario");
    keywordsAsParsedFromJson.put("scenario_outline", "Szenariogrundriss");
    keywordsAsParsedFromJson.put("examples", "Beispiele");
    keywordsAsParsedFromJson.put("given", "*|Angenommen|Gegeben sei");
    keywordsAsParsedFromJson.put("when", "*|Wenn");
    keywordsAsParsedFromJson.put("then", "*|Dann");
    keywordsAsParsedFromJson.put("and", "*|Und");
    keywordsAsParsedFromJson.put("but", "*|Aber");
  }

  @Override
  public Set<String> supportedLanguages() {
    HashSet<String> languages = new HashSet<String>();
    languages.add("de");
    return languages;
  }

  @Override
  public Map<String, String> getRawInputFromI18nJsonFileFor(String isoCode) {
    return Collections.unmodifiableMap(keywordsAsParsedFromJson);
  }
}