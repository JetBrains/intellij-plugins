package org.intellij.plugins.postcss.completion;

import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesSetCompletionTest extends PostCssCompletionTest {

  public void testApply() {
    doTest();
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "customPropertiesSet";
  }
}