package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.jetbrains.plugins.cucumber.i18n.AbstractGherkinKeywordProviderTest;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.io.File;
import java.io.IOException;

public class JsonGherkinKeywordProviderTest extends AbstractGherkinKeywordProviderTest {
  private static final String TEST_DATA_PATH = "/keywords";

  @Override
  protected GherkinKeywordProvider buildKeywordProvider() throws IOException {
    File keywordsFile = new File(getTestDataPath(), "i18n.json");
    return new JsonGherkinKeywordProvider(keywordsFile);
  }

  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + TEST_DATA_PATH;
  }
}
