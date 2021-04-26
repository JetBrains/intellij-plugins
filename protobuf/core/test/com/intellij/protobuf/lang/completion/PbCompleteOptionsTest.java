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
import com.intellij.protobuf.lang.resolve.PbOptionNameReference;

import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/** Tests for option completions (see {@link PbOptionNameReference#getVariants()}). */
public class PbCompleteOptionsTest extends PbCompletionContributorTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.configureByFile("lang/completion/mock_descriptor.proto");
    TestUtils.addTestFileResolveProvider(
        getProject(), "lang/completion/mock_descriptor.proto", getTestRootDisposable());
  }

  public void testFileOption() {
    setInput("option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "file_bool_option",
        "file_int_option",
        "file_string_option",
        "file_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("option file_bool_option");
  }

  public void testMessageOption() {
    setInput("message Foo {", "  option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "message_bool_option",
        "message_int_option",
        "message_string_option",
        "message_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  option message_bool_option");
  }

  public void testFieldOption() {
    setInput("message Foo {", "  int32 x = 1 [" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "field_bool_option",
        "field_int_option",
        "field_string_option",
        "field_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 x = 1 [field_bool_option");
  }

  public void testFieldOptionAfterComma() {
    setInput("message Foo {", "  int32 x = 1 [field_int_option=1, " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "field_bool_option",
        "field_int_option",
        "field_string_option",
        "field_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 x = 1 [field_int_option=1, field_bool_option");
  }

  public void testOneofOption() {
    setInput("message Foo {", "  oneof Bar {", "    option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "oneof_bool_option",
        "oneof_int_option",
        "oneof_string_option",
        "oneof_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  oneof Bar {", "    option oneof_bool_option");
  }

  public void testEnumOption() {
    setInput("enum Foo {", "  option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "enum_bool_option",
        "enum_int_option",
        "enum_string_option",
        "enum_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("enum Foo {", "  option enum_bool_option");
  }

  public void testEnumValueOption() {
    setInput("enum Foo {", "  BAR = 1 [" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "enum_value_bool_option",
        "enum_value_int_option",
        "enum_value_string_option",
        "enum_value_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("enum Foo {", "  BAR = 1 [enum_value_bool_option");
  }

  public void testEnumValueOptionAfterComma() {
    setInput("enum Foo {", "  BAR = 1 [enum_value_int_option=1, " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "enum_value_bool_option",
        "enum_value_int_option",
        "enum_value_string_option",
        "enum_value_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("enum Foo {", "  BAR = 1 [enum_value_int_option=1, enum_value_bool_option");
  }

  public void testServiceOption() {
    setInput("service Foo {", "  option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "service_bool_option",
        "service_int_option",
        "service_string_option",
        "service_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("service Foo {", "  option service_bool_option");
  }

  public void testMethodOption() {
    setInput("service Foo {", "  rpc Bar(int32) returns (int32) {", "    option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "method_bool_option",
        "method_int_option",
        "method_string_option",
        "method_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult(
        "service Foo {", "  rpc Bar(int32) returns (int32) {", "    option method_bool_option");
  }

  public void testStreamOption() {
    setInput("service Foo {", "  stream Bar (int32, int32) {", "    option " + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions,
        "stream_bool_option",
        "stream_int_option",
        "stream_string_option",
        "stream_enum_option");
    assertTrue(completeWithFirstChoice());
    assertResult("service Foo {", "  stream Bar (int32, int32) {", "    option stream_bool_option");
  }

  public void testExtensionOptionUsingGroupUsesLowercaseFieldName() {
    setInput(
        "message MyType {",
        "  group Zz = 1 {",
        "    int32 yy = 1;",
        "  }",
        "}",
        "extend proto2.FileOptions {",
        "  MyType tt = 1;",
        "}",
        "option (tt)." + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "zz");
  }

  public void testCompleteCustomFieldOption() {
    withPackage("com.foo");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "  int32 test_xyz = 2;",
        "}",
        "extend proto2.FieldOptions {",
        "  MyType test_field_option = 2000;",
        "  int32 test_field_option2 = 2001;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType test_message_option = 2000;",
        "}",
        "message TestMessage {",
        "  int32 test_field = 1 [(tes" + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "test_field_option", "test_field_option2");
  }

  public void testCompleteCustomOptionExtendFullyQualified() {
    withPackage("com.foo");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend .proto2.FieldOptions {",
        "  MyType test_field_option = 2000;",
        "  int32 test_field_option2 = 2001;",
        "}",
        "message ExtraMessage {",
        "  int32 test_field = 1 [(tes" + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "test_field_option", "test_field_option2");
  }

  public void testCompleteCustomMessageOption() {
    withPackage("com.foo");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "  int32 test_xyz = 2;",
        "}",
        "extend proto2.FieldOptions {",
        "  MyType test_field_option = 2000;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType test_message_option = 2000;",
        "  int32 test_message_option2 = 2001;",
        "}",
        "message TestMessage {",
        "  option (tes" + CARET_TAG + ")",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "test_message_option", "test_message_option2");
  }

  public void testCompleteQualifierOfCustomOption() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FieldOptions {",
        "  MyType test_field_option = 2000;",
        "}",
        "message TestMessage {",
        "  int32 test_field = 1 [(.fo" + CARET_TAG + ")];",
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FieldOptions {",
        "  MyType test_field_option = 2000;",
        "}",
        "message TestMessage {",
        "  int32 test_field = 1 [(.foosbar.)];",
        "}");
  }

  public void testCompleteFullyQualifiedCustomOption() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "enum MyEnum {}",
        "message ExtensionScope {",
        "  message MoreExtensionScope {",
        "    extend proto2.FieldOptions {",
        "      MyType test_field_option = 2000;",
        "    }",
        "  }",
        "}",
        "extend proto2.FieldOptions {",
        "  MyType test_field_option2 = 2001;",
        "}",
        "message ExtraMessage {",
        "  int32 test_field = 1 [(.foosbar." + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "test_field_option2", "ExtensionScope");
  }

  public void testCompleteRelativeCustomOption() {
    withPackage("foosbar");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "enum MyEnum {}",
        "message ExtensionScope {",
        "  message MoreExtensionScope {",
        "    extend proto2.FieldOptions {",
        "      MyType test_field_option = 2000;",
        "      int32 test_field_option2 = 2001;",
        "    }",
        "  }",
        "  int32 test_field = 1 [(MoreExtensionScope." + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "test_field_option", "test_field_option2");
  }

  public void testCompleteExtendedCustomOption() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional int32 crazy_option_field = 1;",
        "  extensions 50 to max;",
        "}",
        "extend proto2.FieldOptions {",
        "  optional MyType test_field_option = 2000;",
        "}",
        "extend MyType {",
        "  optional int32 crazy_extended_option_field = 1000;",
        "  optional int32 crazy_extended_option_field2 = 1001;",
        "}",
        "message TestMessage {",
        "  optional int32 test_field = 1 [(test_field_option).(cr" + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(
        completions, "crazy_extended_option_field", "crazy_extended_option_field2");
  }

  public void testCompleteQualifierTypeInCustomOption() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  optional int32 crazy_option_field = 1;",
        "  extensions 50 to max;",
        "}",
        "extend proto2.FieldOptions {",
        "  optional MyType test_field_option = 2000;",
        "}",
        "message TestMessage {",
        "  optional int32 test_field = 1 [(test_field_option).(" + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "MyType");
  }

  public void testCompleteDescriptorTypeInCustomOption() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message TestMessage {",
        "  optional int32 test_field = 1 [(proto2." + CARET_TAG + ")];",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "FieldOptions");
  }

  public void testOptionNameCompletionDoesNotSuggestExtensionField() {
    withSyntax("proto2");
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  extend proto2.FieldOptions {",
        "    optional int32 abc = 2005;",
        "  }",
        "}",
        "extend proto2.FieldOptions {",
        "  optional MyType test_field_option = 2000;",
        "}",
        "message TestMessage {",
        "  optional int32 test_field = 1 [(test_field_option)." + CARET_TAG,
        "}");
    assertNoCompletions();
  }

  public void testBlankCustomOptionNoClosingParen() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  bool foo = 2000;",
        "  bool bar = 2001;",
        "}",
        "option (" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "foo", "bar");
  }

  public void testBlankCustomOptionWithClosingParen() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  int32 crazy_option_field = 1;",
        "}",
        "extend proto2.FileOptions {",
        "  bool foo = 2000;",
        "  bool bar = 2001;",
        "}",
        "option (" + CARET_TAG + ")");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "foo", "bar");
  }

  public void testDuplicateOptionNameIsHighlighted() {
    setInput(
        "message TestMessage {",
        "  option message_bool_option = true;",
        "  option " + CARET_TAG,
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsNonhighlightedItems(completions, "message_string_option");
    assertContainsHighlightedItems(completions, "message_bool_option");
  }

  public void testDuplicateExtensionOptionNameIsHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  bool field = 1;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType opt = 2000;",
        "}",
        "message TestMessage {",
        "  option (opt).field = true;",
        "  option (opt)." + CARET_TAG,
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field");
  }

  public void testRepeatedOptionIsNotHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  repeated bool field = 1;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType opt = 2000;",
        "}",
        "message TestMessage {",
        "  option (opt).field = true;",
        "  option (opt)." + CARET_TAG,
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsNonhighlightedItems(completions, "field");
  }

  public void testOtherOneofMembersAreHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  oneof TestOneof {",
        "    bool field1 = 1;",
        "    bool field2 = 2;",
        "  }",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType opt = 2000;",
        "}",
        "message TestMessage {",
        "  option (opt).field1 = true;",
        "  option (opt)." + CARET_TAG,
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "field1", "field2");
  }

  public void testMergeableTypeIsNotHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "message MyType {",
        "  SubType sub = 1;",
        "}",
        "message SubType {",
        "  bool field1 = 1;",
        "  bool field2 = 2;",
        "}",
        "extend proto2.MessageOptions {",
        "  MyType opt = 2000;",
        "}",
        "message TestMessage {",
        "  option (opt).sub.field1 = true;",
        "  option (opt)." + CARET_TAG,
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);

    // The second "sub" should not be highlighted, since it contains children that can be set.
    assertContainsNonhighlightedItems(completions, "sub");
  }

  public void testDuplicateExtensionNameIsHighlighted() {
    setInput(
        "import \"lang/completion/mock_descriptor.proto\";",
        "extend proto2.MessageOptions {",
        "  bool opt = 2000;",
        "  bool opt2 = 2001;",
        "}",
        "message TestMessage {",
        "  option (opt) = true;",
        "  option (o" + CARET_TAG + ")",
        "}");
    List<LookupElement> completions = getCompletionItems();
    assertNotNull(completions);
    assertContainsHighlightedItems(completions, "opt");
    assertContainsNonhighlightedItems(completions, "opt2");
  }
}
