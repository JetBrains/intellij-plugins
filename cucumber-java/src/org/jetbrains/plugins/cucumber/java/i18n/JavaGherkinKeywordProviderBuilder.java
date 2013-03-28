package org.jetbrains.plugins.cucumber.java.i18n;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProviderBuilder;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.io.InputStream;

public class JavaGherkinKeywordProviderBuilder implements GherkinKeywordProviderBuilder {
  @Nullable
  @Override
  public GherkinKeywordProvider getKeywordProvider(Project project) {
    final GherkinKeywordProvider provider = findJsonKeywordProvider();
    if (provider != null) {
      return provider;
    }
    return null;
  }

  @Nullable
  private static GherkinKeywordProvider findJsonKeywordProvider() {
    final InputStream inputStream = JavaGherkinKeywordProviderBuilder.class.getClassLoader().getResourceAsStream("/gherkin/i18n.json");
    if (inputStream != null) {
      return new JsonGherkinKeywordProvider(inputStream);
    }
    return null;
  }
}
