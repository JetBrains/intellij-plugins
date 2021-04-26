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

import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/** Tests for option value completions. */
public class PbCompleteOptionValuesTest extends PbCompletionContributorTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.configureByFile("lang/completion/mock_descriptor.proto");
    TestUtils.addTestFileResolveProvider(
        getProject(), "lang/completion/mock_descriptor.proto", getTestRootDisposable());
  }

  public void testFieldEnumOption() {
    setInput("message Foo {", "  int32 x = 1 [field_enum_option=" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "BAR", "FOO");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 x = 1 [field_enum_option=BAR");
  }

  public void testFieldEnumDefault() {
    setInput(
        "enum Abcd {",
        "  HELLO=1;",
        "  GOODBYE=2;",
        "}",
        "message Foo {",
        "  Abcd x = 1 [default=" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "GOODBYE", "HELLO");
    assertTrue(completeWithFirstChoice());
    assertResult(
        "enum Abcd {",
        "  HELLO=1;",
        "  GOODBYE=2;",
        "}",
        "message Foo {",
        "  Abcd x = 1 [default=GOODBYE");
  }

  public void testFieldBooleanOption() {
    setInput("message Foo {", "  int32 x = 1 [field_bool_option=" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "false", "true");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 x = 1 [field_bool_option=false");
  }

  public void testFieldBooleanDefault() {
    setInput("message Foo {", "  bool x = 1 [default=" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "false", "true");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  bool x = 1 [default=false");
  }
}
