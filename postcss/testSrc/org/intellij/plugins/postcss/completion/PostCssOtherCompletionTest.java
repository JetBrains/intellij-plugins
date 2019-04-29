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

  public void testSimpleVarInterpolationStart() {
    doTestCompletionVariants("foo", "bar", "baz");
  }

  public void testSimpleVarInterpolationMiddle() {
    doTestCompletionVariants("bar", "baz");
  }

  public void testSimpleVarInterpolationEnd() {
    doTest();
  }

  public void testSimpleVarAsSelector() {
    doTestCompletionVariants("foo", "bar", "baz");
  }
}