// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.JSExternalLibraryCompletionFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSCompletionFixture.checkWeHaveInCompletion;
import static com.intellij.lang.javascript.JSExternalLibraryCompletionFixture.jsExternalLibraryCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class FlexExternalLibraryCompletionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSExternalLibraryCompletionFixture> js = jsExternalLibraryCompletionFixture(projectFixture);

  /**
   * Check that completion filtering has no affect on ActionScript
   */
  @Test
  public void testNoFlexCompletionFiltering() {
    LookupElement[] items = js.get().doLibTest("http://jquery-1.4.4.js", "", "as", "NoFlexCompletionFiltering", "NoFlexCompletionFiltering_1");
    checkWeHaveInCompletion(items, "abcFoo");
  }
}
