package com.intellij.lang.javascript.linter.tslint;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class TsLintConfigCompletionTest extends BasePlatformTestCase {


  @Override
  protected String getTestDataPath() {
    return TsLintTestUtil.BASE_TEST_DATA_PATH + "/config/completion";
  }

  public void testTopLevelRules() {
    List<String> result = myFixture.getCompletionVariants("/topLevelRules/tslint.json");
    final String[] variants = {"\"max-classes-per-file\"", "\"max-file-line-count\"",
      "\"max-line-length\""};
    assertContainsOrdered(result, variants);
  }

  public void testMemberOrdering() {
    List<String> result = myFixture.getCompletionVariants("/memberOrdering/tslint.json");
    final String[] variants = {"\"fields-first\"", "\"instance-sandwich\"", "\"statics-first\""};
    assertContainsOrdered(result, variants);
  }

  public void testTypedefWhitespace() {
    List<String> result = myFixture.getCompletionVariants("/typedefWhitespace/tslint.json");
    final String[] variants = {"\"call-signature\"", "\"index-signature\"", "\"parameter\"", "\"property-declaration\"",
      "\"variable-declaration\""};
    assertContainsOrdered(result, variants);
  }

  public void testTypedefWhitespaceValues() {
    List<String> result = myFixture.getCompletionVariants("/typedefWhitespaceValues/tslint.json");
    final String[] variants = {"nospace", "space"};

    assertContainsOrdered(result, variants);
  }
}
