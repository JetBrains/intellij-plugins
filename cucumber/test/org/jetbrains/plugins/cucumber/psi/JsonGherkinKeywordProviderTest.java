package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.plugins.cucumber.i18n.AbstractGherkinKeywordProviderTest;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.io.InputStream;

public class JsonGherkinKeywordProviderTest extends AbstractGherkinKeywordProviderTest {
  @Override
  protected GherkinKeywordProvider buildKeywordProvider() {
    ClassLoader classLoader = JsonGherkinKeywordProvider.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("i18n.json");
    return new JsonGherkinKeywordProvider(inputStream);
  }
}
