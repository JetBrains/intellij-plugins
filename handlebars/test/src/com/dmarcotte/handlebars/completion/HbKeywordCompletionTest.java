package com.dmarcotte.handlebars.completion;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.Arrays;

public class HbKeywordCompletionTest extends LightPlatformCodeInsightFixtureTestCase {
  public void doBasicTest(String text, String... expected) {
    myFixture.configureByText(HbFileType.INSTANCE, text);
    myFixture.complete(CompletionType.BASIC);
    assertContainsElements(myFixture.getLookupElementStrings(), expected);
  }

  public void testSimple() {
    doBasicTest("{{#<caret>}}", "if", "each");
  }
}
