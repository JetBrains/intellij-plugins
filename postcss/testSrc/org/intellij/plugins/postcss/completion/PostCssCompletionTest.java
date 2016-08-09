package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.Pair;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

public abstract class PostCssCompletionTest extends PostCssFixtureTestCase {
  protected void doTestPreferred(String... items) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, items);
  }

  protected void doTestPreferred(Pair<String, String>... items) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    LookupElement[] lookupElements = myFixture.completeBasic();
    LookupElementPresentation presentation = new LookupElementPresentation();
    for (int i = 0; i < items.length; i++) {
      lookupElements[i].renderElement(presentation);
      assertEquals(items[i].first, presentation.getItemText());
      assertEquals(items[i].second, presentation.getTypeText());
    }
  }

  protected void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }
}