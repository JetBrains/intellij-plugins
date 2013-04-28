package com.dmarcotte.handlebars.completion;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class HbKeywordCompletionTest extends LightPlatformCodeInsightFixtureTestCase {
  public void doBasicTest(String text, String... expected) {
    myFixture.configureByText(HbFileType.INSTANCE, text);
    myFixture.complete(CompletionType.BASIC);
    assertContainsElements(myFixture.getLookupElementStrings(), expected);
  }

  public void testMustacheCompletions() {
    doBasicTest("{{<caret>}}", "log");
  }

  public void testBlockCompletions() {
    doBasicTest("{{#<caret>}}", "if", "each", "unless", "with");
  }

  public void testElseCompletion() {
    doBasicTest("{{#if}}{{<caret>}}{{/if}}", "else", "log");
  }
}
