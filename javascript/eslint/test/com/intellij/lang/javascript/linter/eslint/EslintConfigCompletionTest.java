package com.intellij.lang.javascript.linter.eslint;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class EslintConfigCompletionTest extends BasePlatformTestCase {
  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/config/completion";
  }

  public void testRules() {
    List<String> result = getCompletionVariants();
    assertContainsElements(result, "\"semi\"");
  }

  public void testRulesInOverrides() {
    List<String> result = getCompletionVariants();
    assertContainsElements(result, "\"semi\"");
  }

  private List<String> getCompletionVariants() {
    return myFixture.getCompletionVariants(getTestName(true) + "/.eslintrc.json");
  }
}
