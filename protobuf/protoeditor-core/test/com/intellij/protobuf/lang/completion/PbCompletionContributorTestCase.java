/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.completion;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.PbFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for {@link PbCompletionContributor} tests. Provides useful utilities for setup,
 * actions, and assertions.
 */
public abstract class PbCompletionContributorTestCase extends PbCodeInsightFixtureTestCase {

  private String syntaxForTest = "proto3";
  private String packageForTest = "plugin.test";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
  }

  /** Override the default syntax for the test. */
  void withSyntax(String syntaxForTest) {
    this.syntaxForTest = syntaxForTest;
  }

  /** Override the default package for the test. */
  void withPackage(String packageForTest) {
    this.packageForTest = packageForTest;
  }

  PsiFile setInput(String... fileContents) {
    return myFixture.configureByText(
        PbFileType.INSTANCE,
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
  }

  PsiFile setAdditionalFile(String filename, String... fileContents) {
    return myFixture.configureByText(
        filename,
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
  }

  void assertResult(String... resultingFileContents) {
    myFixture.checkResult(
        TestUtils.makeFileWithSyntaxAndPackage(
            syntaxForTest, packageForTest, resultingFileContents));
  }

  /**
   * @return null if the only item was auto-completed
   * @see com.intellij.testFramework.fixtures.CodeInsightTestFixture#completeBasic()
   */
  @Nullable
  List<LookupElement> getCompletionItems() {
    LookupElement[] completionItems = myFixture.completeBasic();
    if (completionItems == null) {
      return null;
    }
    return Arrays.asList(completionItems);
  }

  /**
   * @return null if the only item was auto-completed
   * @see com.intellij.testFramework.fixtures.CodeInsightTestFixture#completeBasic()
   */
  @Nullable
  List<String> getCompletionItemsAsStrings() {
    LookupElement[] completionItems = myFixture.completeBasic();
    if (completionItems == null) {
      return null;
    }
    return getCompletionItemsAsStrings(Arrays.stream(completionItems));
  }

  List<String> getCompletionItemsAsStrings(Stream<LookupElement> lookupElements) {
    return lookupElements.map(LookupElement::getLookupString).collect(Collectors.toList());
  }

  /**
   * Choose the unique completion and apply it, if it exists.
   *
   * @return true if completion is applied (by auto-complete or choice)
   */
  boolean completeWithUniqueChoice() {
    LookupElement[] completionItems = myFixture.completeBasic();
    if (completionItems == null) {
      return true;
    }
    if (completionItems.length != 1) {
      return false;
    }
    myFixture.getLookup().setCurrentItem(completionItems[0]);
    myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR);
    return true;
  }

  /**
   * Choose the first completion and apply it, if it exists.
   *
   * @return true if completion is applied (by auto-complete or choice)
   */
  boolean completeWithFirstChoice() {
    LookupElement[] completionItems = myFixture.completeBasic();
    if (completionItems == null) {
      return true;
    }
    if (completionItems.length == 0) {
      return false;
    }
    myFixture.getLookup().setCurrentItem(completionItems[0]);
    myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR);
    return true;
  }

  void assertNoCompletions() {
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertEmpty(completions);
  }

  public static void assertContainsItems(
      @NotNull Collection<? extends LookupElement> collection, @NotNull String... expected) {
    List<String> items =
        collection.stream().map(LookupElement::getLookupString).collect(Collectors.toList());
    assertContainsElements(items, Arrays.asList(expected));
  }

  public static void assertContainsHighlightedItems(
      @NotNull Collection<? extends LookupElement> collection, @NotNull String... expected) {
    List<LookupElement> highlightedItems = new ArrayList<>();
    for (LookupElement lookupElement : collection) {
      LookupElementPresentation presentation = new LookupElementPresentation();
      Color defaultColor = presentation.getItemTextForeground();
      lookupElement.renderElement(presentation);
      if (!defaultColor.equals(presentation.getItemTextForeground())) {
        highlightedItems.add(lookupElement);
      }
    }
    assertContainsItems(highlightedItems, expected);
  }

  public static void assertContainsNonhighlightedItems(
      @NotNull Collection<? extends LookupElement> collection, @NotNull String... expected) {
    List<LookupElement> items = new ArrayList<>();
    for (LookupElement lookupElement : collection) {
      LookupElementPresentation presentation = new LookupElementPresentation();
      Color defaultColor = presentation.getItemTextForeground();
      lookupElement.renderElement(presentation);
      if (defaultColor.equals(presentation.getItemTextForeground())) {
        items.add(lookupElement);
      }
    }
    assertContainsItems(items, expected);
  }
}
