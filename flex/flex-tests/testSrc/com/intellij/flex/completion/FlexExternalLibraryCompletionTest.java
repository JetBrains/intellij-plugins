// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.JSExternalLibraryCompletionTestBase;

public class FlexExternalLibraryCompletionTest extends JSExternalLibraryCompletionTestBase {

  /**
   * Check that completion filtering has no affect on ActionScript
   */
  public void testNoFlexCompletionFiltering() {
    LookupElement[] items = doLibTest("http://jquery-1.4.4.js", "", "as", "NoFlexCompletionFiltering", "NoFlexCompletionFiltering_1");
    checkWeHaveInCompletion(items, "abcFoo");
  }
}
