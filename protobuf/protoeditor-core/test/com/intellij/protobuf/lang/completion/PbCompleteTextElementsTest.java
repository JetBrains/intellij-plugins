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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.protobuf.TestUtils;

import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/** Tests for injected text format completions. */
public class PbCompleteTextElementsTest extends PbCompletionContributorTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.configureByFile("lang/completion/mock_descriptor.proto");
    TestUtils.addTestFileResolveProvider(
        getProject(), "lang/completion/mock_descriptor.proto", getTestRootDisposable());
  }

  public void testFieldCompletion() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  cr" + CARET_TAG + ": 1",
        "};");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  crazy_option_field: 1",
        "};");
  }

  public void testGroupFieldCompletionSuggestsTypeName() {
    withPackage("foosbar");
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional int32 foo = 2;",
        "  optional group MyGroup = 1 {",
        "    optional int32 xyz = 1;",
        "  }",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  " + CARET_TAG,
        "};");

    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsItems(completions, "foo", "MyGroup");
  }

  public void testFieldCompletionDoesNotSuggestExtensionField() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "  extend proto2.FileOptions {",
        "    int32 xyz = 1004;",
        "  }",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  x" + CARET_TAG,
        "};");
    assertNoCompletions();
  }

  public void testEnumValueCompletion() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "enum MyEnum {",
        "  FOO = 1;",
        "  BAR = 2;",
        "}",
        "message MyType {",
        "  MyEnum enum_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  enum_field: F" + CARET_TAG,
        "};");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "import \"lang/completion/mock_descriptor.proto\";",
        "enum MyEnum {",
        "  FOO = 1;",
        "  BAR = 2;",
        "}",
        "message MyType {",
        "  MyEnum enum_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  enum_field: FOO",
        "};");
  }

  public void testExtensionNameCompletion() {
    // Only 'ext_opt' should be completed, since 'ext_other_opt' does not extend MyType.
    withPackage("foosbar");
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  extensions 100 to 200;",
        "}",
        "message MyOtherType {",
        "  extensions 100 to 200;",
        "}",
        "extend MyType {",
        "  optional int32 ext_opt = 150;",
        "}",
        "extend MyOtherType {",
        "  optional int32 ext_other_opt = 150;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  [ex" + CARET_TAG + "]: 1",
        "};");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  extensions 100 to 200;",
        "}",
        "message MyOtherType {",
        "  extensions 100 to 200;",
        "}",
        "extend MyType {",
        "  optional int32 ext_opt = 150;",
        "}",
        "extend MyOtherType {",
        "  optional int32 ext_other_opt = 150;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  [ext_opt]: 1",
        "};");
  }

  public void testSubMessageExtensionNameCompletion() {
    // Only 'ext_opt' should be completed, since 'ext_other_opt' does not extend MyType.
    withPackage("foosbar");
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  extensions 100 to 200;",
        "  optional MySubType sub_type = 1;",
        "}",
        "message MySubType {",
        "  extensions 100 to 200;",
        "}",
        "message MyOtherType {",
        "  extensions 100 to 200;",
        "}",
        "extend MyType {",
        "  optional int32 ext_opt = 150;",
        "}",
        "extend MySubType {",
        "  optional int32 ext_sub_opt = 150;",
        "}",
        "extend MyOtherType {",
        "  optional int32 ext_other_opt = 150;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  sub_type {",
        "    [ex" + CARET_TAG + "]: 1",
        "  }",
        "};");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  extensions 100 to 200;",
        "  optional MySubType sub_type = 1;",
        "}",
        "message MySubType {",
        "  extensions 100 to 200;",
        "}",
        "message MyOtherType {",
        "  extensions 100 to 200;",
        "}",
        "extend MyType {",
        "  optional int32 ext_opt = 150;",
        "}",
        "extend MySubType {",
        "  optional int32 ext_sub_opt = 150;",
        "}",
        "extend MyOtherType {",
        "  optional int32 ext_other_opt = 150;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  sub_type {",
        "    [ext_sub_opt]: 1",
        "  }",
        "};");
  }

  public void testExtensionNameCompletionDoesNotSuggestMembers() {
    // In text format, extension names must refer to actual extension fields. The completion below
    // should not suggest the member in MyType.
    withPackage("foosbar");
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional string member = 20;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  [MyType." + CARET_TAG,
        "};");
    assertNoCompletions();
  }

  public void testDuplicateFieldNameIsHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  bool field = 1;",
        "  bool foobar = 2;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  field: true",
        "  f" + CARET_TAG,
        "};");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field");
    assertContainsNonhighlightedItems(completions, "foobar");
  }

  public void testDuplicateFieldNameInNestedMessageIsHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  bool field = 1;",
        "  bool foobar = 2;",
        "  MyType recurse = 3;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  recurse {",
        "    field: true",
        "    f" + CARET_TAG,
        "  }",
        "};");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field");
    assertContainsNonhighlightedItems(completions, "foobar");
  }

  public void testDuplicateFieldAndOptionNamesAreHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  bool field = 1;",
        "  bool foobar = 2;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  f" + CARET_TAG,
        "};",
        "option (opt).field = true;");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field");
    assertContainsNonhighlightedItems(completions, "foobar");
  }

  public void testRepeatedOptionIsNotHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  repeated bool field = 1;",
        "  bool foobar = 2;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  field: true",
        "  f" + CARET_TAG,
        "};");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsNonhighlightedItems(completions, "field", "foobar");
  }

  public void testOtherOneofMembersAreHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  oneof TestOneof {",
        "    bool field1 = 1;",
        "    bool field2 = 2;",
        "  }",
        "  bool foobar = 3;",
        "}",
        "extend proto2.FileOptions {",
        "  MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  field1: true",
        "  f" + CARET_TAG,
        "};");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field1", "field2");
    assertContainsNonhighlightedItems(completions, "foobar");
  }

  public void testDuplicateExtensionNameIsHighlighted() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional bool field = 1;",
        "  extensions 100 to max;",
        "}",
        "extend MyType {",
        "  optional bool ext = 2000;",
        "  optional bool ext2 = 2001;",
        "}",
        "extend proto2.FileOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  [ext]: true",
        "  [e" + CARET_TAG,
        "};");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "ext");
    assertContainsNonhighlightedItems(completions, "ext2");
  }

  public void testDuplicateGroupFieldsWithDifferentCasingAreHighlighted() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional group MyGroup = 1 {",
        "    optional int32 xyz = 1;",
        "  }",
        "  optional int32 foobar = 2;",
        "}",
        "extend proto2.MessageOptions {",
        "  optional MyType opt = 2000;",
        "}",
        "option (opt) = {",
        "  " + CARET_TAG,
        "};",
        "option (opt).mygroup.xyz =1;");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "MyGroup");
    assertContainsNonhighlightedItems(completions, "foobar");
  }
}
