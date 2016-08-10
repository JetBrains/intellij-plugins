package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesSetCompletionTest extends PostCssCompletionTest {

  public void testApply() {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    LookupElement[] lookupElements = myFixture.completeBasic();
    assertEquals("@apply", lookupElements[0].getLookupString());
    assertEquals("@apply", lookupElements[1].getLookupString());
    assertTrue(lookupElements[0].toString().contains("PostCssOneLineAtRuleInsertHandler"));
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customPropertiesSet";
  }
}