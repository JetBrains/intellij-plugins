package org.jetbrains.plugins.cucumber.java.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GherkinDictionaryKeywordProvider implements GherkinKeywordProvider{
  private final GherkinDictionary myDictionary;

  public GherkinDictionaryKeywordProvider(GherkinDictionary dictionary) {
    myDictionary = dictionary;
  }

  @Override
  public Collection<String> getAllKeywords(String language) {
    return mappingFor(language).keySet();
  }

  @Override
  public IElementType getTokenType(String language, String keyword) {
    return mappingFor(language).get(keyword);
  }

  @Override
  public String getBaseKeyword(String language, String keyword) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSpaceAfterKeyword(String language, String keyword) {
    return myDictionary.isSpaceAfterKeyword(language, keyword);
  }

  @Override
  public boolean isStepKeyword(String keyword) {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public GherkinKeywordTable getKeywordsTable(@Nullable String language) {
    return keywordTableFor(language);
  }

  private Map<String, IElementType> mappingFor(String language) {
    Map<String, IElementType> mapping = new HashMap<String, IElementType>();
    keywordTableFor(language).putAllKeywordsInto(mapping);
    return mapping;
  }

  private GherkinKeywordTable keywordTableFor(String language) {
    return myDictionary.forLanguage(language);
  }
}