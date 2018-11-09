package org.intellij.plugins.postcss.completion;

import org.jetbrains.annotations.NotNull;

public class PostCssNestingCompletionTest extends PostCssCompletionTest {
  public void testPropertiesBeforeTag() {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    assertEmpty(myFixture.completeBasic());
  }

  public void testPropertiesLineBeforeTag() {
    doTestPreferred("padding");
  }

  public void testTagsBeforeProperty() {
    doTest();
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "nestingRuleset";
  }
}
