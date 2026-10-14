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

import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.lang.resolve.PbImportReference;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Test for import file path completions (see {@link PbImportReference#getVariants()}). */
@TestApplication
public class PbCompleteImportsTest extends PbCompletionContributorTestCase {

  @BeforeEach
  void setUp() {
    TestUtils.addTestFileResolveProvider(getProject(), testDisposable);
    myFixture.addFileToProject("foo/baaaz/import_me.proto", "message Abc {}");
    myFixture.addFileToProject("foo/benjamin/franklin.proto", "message Xyz {}");
  }

  @Test
  public void testCompleteNoSlash() {
    setInput("import \"" + CARET_TAG);
    Collection<String> results = TestUtils.notNull(getCompletionItemsAsStrings());
    // At the root, there's at least foo, but there's also the temp file for the input,
    // which we don't check.
    assertThat(results).contains("foo/");
  }

  @Test
  public void testCompleteOneSlash() {
    setInput("import \"foo/b" + CARET_TAG);
    Collection<String> results = TestUtils.notNull(getCompletionItemsAsStrings());
    assertThat(results).containsExactly("foo/baaaz/", "foo/benjamin/");
  }

  @Test
  public void testCompleteEndsWithSlash() {
    setInput("import \"foo/baaaz/" + CARET_TAG + "\"");
    assertTrue(completeWithUniqueChoice());
    assertResult("import \"foo/baaaz/import_me.proto\"");
  }

  @Test
  public void testCompleteNoResults() {
    setInput("import \"nothing_here" + CARET_TAG);
    assertNoCompletions();
  }
}
