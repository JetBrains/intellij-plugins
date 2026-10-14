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
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for {@link PbCompletionContributor} tests. Provides useful utilities for setup,
 * actions, and assertions.
 */
public abstract class PbCompletionContributorTestCase {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  protected CodeInsightTestFixture myFixture;
  protected Disposable testDisposable;

  private String syntaxForTest = "proto3";
  private String packageForTest = "plugin.test";

  @BeforeEach
  void setUpCompletionTestCase(@TestDisposable Disposable disposable) {
    myFixture = codeInsightFixture.get();
    testDisposable = disposable;
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    TestUtils.addTestFileResolveProvider(myFixture.getProject(), testDisposable);
  }

  protected Project getProject() {
    return myFixture.getProject();
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
    // Other files import this file by its name, so keep the exact name.
    PsiFile file = myFixture.addFileToProject(
        filename,
        TestUtils.makeFileWithSyntaxAndPackage(syntaxForTest, packageForTest, fileContents));
    myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
    return file;
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
    LookupElement[] completionItems = EdtTestUtil.runInEdtAndGet(() -> myFixture.completeBasic());
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
    LookupElement[] completionItems = EdtTestUtil.runInEdtAndGet(() -> myFixture.completeBasic());
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
    return EdtTestUtil.runInEdtAndGet(() -> {
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
    });
  }

  /**
   * Choose the first completion and apply it, if it exists.
   *
   * @return true if completion is applied (by auto-complete or choice)
   */
  boolean completeWithFirstChoice() {
    return EdtTestUtil.runInEdtAndGet(() -> {
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
    });
  }

  void assertNoCompletions() {
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertThat(completions).isEmpty();
  }

  public static void assertContainsItems(
      @NotNull Collection<? extends LookupElement> collection, @NotNull String... expected) {
    List<String> items =
        collection.stream().map(LookupElement::getLookupString).collect(Collectors.toList());
    assertThat(items).containsAllIn(expected);
  }

  public static void assertContainsHighlightedItems(
      @NotNull Collection<? extends LookupElement> collection, @NotNull String... expected) {
    List<LookupElement> highlightedItems = new ArrayList<>();
    for (LookupElement lookupElement : collection) {
      LookupElementPresentation presentation = new LookupElementPresentation();
      Color defaultColor = presentation.getItemTextForeground();
      Color color = ReadAction.computeBlocking(() -> {
        lookupElement.renderElement(presentation);
        return presentation.getItemTextForeground();
      });
      if (!defaultColor.equals(color)) {
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
      Color color = ReadAction.computeBlocking(() -> {
        lookupElement.renderElement(presentation);
        return presentation.getItemTextForeground();
      });
      if (defaultColor.equals(color)) {
        items.add(lookupElement);
      }
    }
    assertContainsItems(items, expected);
  }
}
