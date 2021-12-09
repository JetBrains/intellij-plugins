package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtil;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class PostCssCompletionTest extends PostCssFixtureTestCase {
  protected void doTestCompletionVariants(String... items) {
    myFixture.testCompletionVariants(getTestName(true) + ".pcss", items);
  }

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

  protected void doTestPreferredNotStrict(final Pair<String, String> @NotNull ... expectedItems) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    final LookupElement[] lookupElements = myFixture.completeBasic();
    final LookupElementPresentation presentation = new LookupElementPresentation();
    double priority = 0;
    for (int i = 0; i < expectedItems.length; i++) {
      final LookupElement lookupElement = lookupElements[i];
      if (lookupElement instanceof PrioritizedLookupElement<?>) {
        final double p = ((PrioritizedLookupElement<?>)lookupElement).getPriority();
        if (priority != 0 && priority != p) {
          fail("There are lookup elements with different priorities among top " + expectedItems.length + " completion items");
        }
        priority = p;
      }

      lookupElement.renderElement(presentation);
      final Pair<String, String> actual = Pair.create(presentation.getItemText(), presentation.getTypeText());
      assertTrue(actual + " is not expected among top " + expectedItems.length + " completion items",
                 ArrayUtil.contains(actual, expectedItems));
    }
  }

  protected void doTest() {
    myFixture.testCompletion(getTestName(true) + ".pcss", getTestName(true) + "_after.pcss");
  }
}