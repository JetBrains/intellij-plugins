package org.jetbrains.plugins.cucumber.java.i18n;

import org.jetbrains.plugins.cucumber.i18n.AbstractGherkinKeywordProviderTest;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.io.IOException;

/**
 * User: Andrey.Vokin
 * Date: 3/28/13
 */
public class JavaJsonGherkinProviderTest extends AbstractGherkinKeywordProviderTest {
  @Override
  protected GherkinKeywordProvider buildKeywordProvider() throws IOException {
    return new JsonGherkinKeywordProvider(JavaJsonGherkinProviderTest.class.getClassLoader().getResourceAsStream("gherkin/i18n.json"));
  }
}
