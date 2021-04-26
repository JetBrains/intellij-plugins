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
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.PbTextFileType;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Tests completion scenarios for text format schema comments. */
public class PbTextDirectiveCompletionTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
    myFixture.configureByFile("lang/resolve/root_message.proto");
    myFixture.configureByFile("lang/resolve/other_message.proto");
  }

  public void testCommentPrefixCompletions() {
    List<String> completions = completeStrings("# <caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  public void testCommentPrefixCompletionsAfterExistingTokenPrefix() {
    List<String> completions = completeStrings("# pro<caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  public void testCommentPrefixCompletionsBeforeExistingToken() {
    List<String> completions = completeStrings("# <caret> foo");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  public void testNoCompletionsAfterExistingToken() {
    List<String> completions = completeStrings("# foo <caret>");
    assertThat(completions).isEmpty();
  }

  public void testAlreadySpecifiedCommentsAreNotSuggested() {
    List<String> completions = completeStrings("# proto-file: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-message: ", "proto-import: ");
    assertThat(completions).doesNotContain("proto-file: ");

    completions = completeStrings("# proto-message: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-import: ");
    assertThat(completions).doesNotContain("proto-message: ");
  }

  public void testMultipleImports() {
    List<String> completions =
        completeStrings("# proto-file: foo\n# proto-message: bar\n# proto-import: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-import: ");
  }

  public void testFileCompletion() {
    List<String> completions = completeStrings("# proto-file: lang/resolve/<caret>");
    assertThat(completions)
        .containsExactly("lang/resolve/root_message.proto", "lang/resolve/other_message.proto");
  }

  public void testMessageCompletion() {
    List<String> completions =
        completeStrings(
            "# proto-file: lang/resolve/root_message.proto\n" + "# proto-message: <caret>");
    assertThat(completions).containsExactly("foo.", "bar.", "Message");
  }

  private List<String> completeStrings(String text) {
    myFixture.configureByText(PbTextFileType.INSTANCE, text);
    myFixture.complete(CompletionType.BASIC, 1);
    return myFixture.getLookupElementStrings();
  }
}
