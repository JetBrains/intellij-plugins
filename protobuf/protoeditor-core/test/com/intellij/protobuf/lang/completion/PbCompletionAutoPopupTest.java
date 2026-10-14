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
import com.intellij.protobuf.TestUtils;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTestUtil;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTester;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/** Tests for autopop of completion suggestions */
@TestApplication
public class PbCompletionAutoPopupTest extends PbCompletionContributorTestCase {

  private CompletionAutoPopupTester tester;

  @BeforeEach
  void setUp() {
    this.tester = new CompletionAutoPopupTester(myFixture);
    TestUtils.addTestFileResolveProvider(getProject(), testDisposable);
    myFixture.addFileToProject("foo/baaaz/import_me.proto", "message Abc {}");
  }

  private <E extends Throwable> void runTest(@NotNull ThrowableRunnable<E> runnable) throws E {
    CompletionAutoPopupTestUtil.runWithAutoPopupEnabled(myFixture, runnable);
  }

  @Test
  public void testNoAutoPopupFieldNumber() {
    runTest(() -> {
      setInput("message Foo {", "  int64 foo = 4" + CARET_TAG, "}");
      tester.typeWithPauses("5");
      assertThat(tester.getLookup()).isNull();
    });
  }

  @Test
  public void testNoAutoPopupReservedNumber() {
    runTest(() -> {
      setInput("message Foo {", "  reserved 1, 4 to 5" + CARET_TAG, "}");
      tester.typeWithPauses("6");
      assertThat(tester.getLookup()).isNull();
    });
  }

  @Test
  public void testAutoPopupOnImportSlash() {
    runTest(() -> {
      setInput("import \"foo/baaaz" + CARET_TAG + "\"");
      tester.typeWithPauses("/");
      Lookup lookup = tester.getLookup();
      assertThat(lookup).isNotNull();
      assertThat(getCompletionItemsAsStrings(lookup.getItems().stream()))
          .containsExactly("foo/baaaz/import_me.proto");
    });
  }
}
