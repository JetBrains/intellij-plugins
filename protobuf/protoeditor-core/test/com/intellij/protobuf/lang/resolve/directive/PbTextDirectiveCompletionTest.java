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
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbTextFileType;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests completion scenarios for text format schema comments. */
@TestApplication
public class PbTextDirectiveCompletionTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp(@TestDisposable Disposable disposable) {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    TestUtils.addTestFileResolveProvider(myFixture.getProject(), disposable);
    myFixture.configureByFile("lang/resolve/root_message.proto");
    myFixture.configureByFile("lang/resolve/other_message.proto");
  }

  @Test
  public void testCommentPrefixCompletions() {
    List<String> completions = completeStrings("# <caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  @Test
  public void testCommentPrefixCompletionsAfterExistingTokenPrefix() {
    List<String> completions = completeStrings("# pro<caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  @Test
  public void testCommentPrefixCompletionsBeforeExistingToken() {
    List<String> completions = completeStrings("# <caret> foo");
    assertThat(completions).containsExactly("proto-file: ", "proto-message: ", "proto-import: ");
  }

  @Test
  public void testNoCompletionsAfterExistingToken() {
    List<String> completions = completeStrings("# foo <caret>");
    assertThat(completions).isEmpty();
  }

  @Test
  public void testAlreadySpecifiedCommentsAreNotSuggested() {
    List<String> completions = completeStrings("# proto-file: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-message: ", "proto-import: ");
    assertThat(completions).doesNotContain("proto-file: ");

    completions = completeStrings("# proto-message: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-file: ", "proto-import: ");
    assertThat(completions).doesNotContain("proto-message: ");
  }

  @Test
  public void testMultipleImports() {
    List<String> completions =
        completeStrings("# proto-file: foo\n# proto-message: bar\n# proto-import: foo\n# <caret>");
    assertThat(completions).containsExactly("proto-import: ");
  }

  @Test
  public void testFileCompletion() {
    List<String> completions = completeStrings("# proto-file: lang/resolve/<caret>");
    assertThat(completions)
        .containsExactly("lang/resolve/root_message.proto", "lang/resolve/other_message.proto");
  }

  @Test
  public void testMessageCompletion() {
    List<String> completions =
        completeStrings(
            "# proto-file: lang/resolve/root_message.proto\n" + "# proto-message: <caret>");
    assertThat(completions).containsExactly("foo.", "bar.", "Message");
  }

  private List<String> completeStrings(String text) {
    myFixture.configureByText(PbTextFileType.INSTANCE, text);
    return EdtTestUtil.runInEdtAndGet(() -> {
      myFixture.complete(CompletionType.BASIC, 1);
      return myFixture.getLookupElementStrings();
    });
  }
}
