package org.intellij.plugins.postcss.completion;

import org.jetbrains.annotations.NotNull;

public class PostCssOtherCompletionTest extends PostCssCompletionTest {

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "other";
  }

  public void testSimpleVarAsPropertyValue() {
    doTestCompletionVariants("foo", "bar", "baz");
  }

  public void testSimpleVarAsSelector() {
    doTestCompletionVariants("foo", "bar", "baz");
  }
}