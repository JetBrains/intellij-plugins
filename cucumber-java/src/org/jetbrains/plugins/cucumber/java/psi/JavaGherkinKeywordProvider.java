package org.jetbrains.plugins.cucumber.java.psi;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProviderBuilder;

public class JavaGherkinKeywordProvider implements GherkinKeywordProviderBuilder {
  private static final GherkinDictionary dictionary = new GherkinDictionary(new GherkinI18nJsonToKeywordTable(), GherkinJsonLoader.loadLanguagesFromGherkinLibrary());
  @Nullable
  @Override
  public GherkinKeywordProvider getKeywordProvider(Project project) {
    return new GherkinDictionaryKeywordProvider(dictionary);
  }
}
