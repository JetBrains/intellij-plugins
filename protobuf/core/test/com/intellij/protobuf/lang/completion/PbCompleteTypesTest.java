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

import com.intellij.protobuf.lang.resolve.ProtoSymbolPathReference;

import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/**
 * Tests for completing types. See {@link PbCompletionContributor} for builtin types, and see {@link
 * ProtoSymbolPathReference#getVariants()} for
 * user-defined types.
 */
public class PbCompleteTypesTest extends PbCompletionContributorTestCase {

  public void testIntInField() {
    setInput("message Foo {", "  in" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "int32", "int64", "sint32", "sint64", "uint32", "uint64");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 ");
  }

  public void testIntInFieldWithCloseBrace() {
    setInput("message Foo {", "  in" + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "int32", "int64", "sint32", "sint64", "uint32", "uint64");
    assertTrue(completeWithFirstChoice());
    assertResult("message Foo {", "  int32 ", "}");
  }

  public void testPrimitiveTypeInFieldAfterRepeatedKeyword() {
    setInput("message Foo {", "  repeated in" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "int32", "int64", "sint32", "sint64", "uint32", "uint64");
  }

  public void testPrimitiveTypeInFieldAfterOptionalKeyword() {
    withSyntax("proto2");
    setInput("message Foo {", "  optional in" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertContainsElements(completions, "int32", "int64", "sint32", "sint64", "uint32", "uint64");
  }

  public void testNoInt32CompletionInFieldIdentifier() {
    setInput("message Foo {", "  int32 in" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoInt32CompletionInMessageIdentifier() {
    setInput("message in" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoInt32CompletionAsEnum() {
    setInput("enum AnEnum {", "  in" + CARET_TAG);
    assertNoCompletions();
  }

  public void testFloatInField() {
    setInput("message Foo {", "  fl" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  float ");
  }

  public void testDoubleInField() {
    setInput("message Foo {", "  do" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  double ");
  }

  public void testNoDoubleCompletionInComment() {
    setInput("message Foo {", "  // doub" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoDoubleCompletionInFieldOption() {
    setInput("message Foo {", "  int32 some_field = 1 [(dou" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoDoubleCompletionInExtend() {
    setInput("extend dou" + CARET_TAG);
    assertNoCompletions();
  }

  public void testBoolBytesInField() {
    setInput("message Foo {", "  b" + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "bool", "bytes", "double");
  }

  public void testPrimitiveTypeInMapKeyType() {
    setInput("message Foo {", "  map<do" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  map<double");
  }

  public void testPrimitiveTypeInMapKeyTypeWithCloseBraces() {
    setInput("message Foo {", "  map<do" + CARET_TAG + ">", "}");
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  map<double>", "}");
  }

  public void testPrimitiveTypeInMapValueType() {
    setInput("message Foo {", "  map<int32, do" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  map<int32, double");
  }

  public void testNoPrimitiveTypeInServiceMethodParam() {
    setInput("service Foo {", "  rpc Bar(dou" + CARET_TAG + ")", "}");
    assertNoCompletions();
  }

  public void testNoPrimitiveTypeInServiceMethodReturn() {
    setInput(
        "message Message {}",
        "service Foo {",
        "  rpc Bar(Message) returns (dou" + CARET_TAG + ")",
        "}");
    assertNoCompletions();
  }

  // User-defined types.

  public void testNonQualifiedTypeFileScope() {
    setInput(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "  int32 MyField2 = 2;",
        "  extensions 500 to max;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "  BAR = 2;",
        "}",
        "extend My" + CARET_TAG);
    // Given message, enum, and field with "My", should only be able to extend the message.
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "  int32 MyField2 = 2;",
        "  extensions 500 to max;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "  BAR = 2;",
        "}",
        "extend MyMessage");
  }

  public void testNonQualifiedTypeMessageScope() {
    setInput(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  UNKNOWN = 0;",
        "  FOO = 1;",
        "  BAZ = 2;",
        "}",
        "message Foo {",
        "  int32 MyField2 = 1;",
        "  My" + CARET_TAG,
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "MyEnum", "MyMessage");
  }

  public void testNonQualifiedTypeDeclaredInNonScopeCreatingParent() {
    withSyntax("proto2");
    setInput(
        "message MyMessage {",
        "  oneof my_oneof {",
        "    group MyGroup = 1 {",
        "      optional int32 my_group_field = 1;",
        "    }",
        "  }",
        "  My" + CARET_TAG,
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "MyMessage", "MyGroup");
  }

  public void testNonQualifiedTypeNestedMessageScope() {
    setInput(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  UNKNOWN = 0;",
        "  FOO = 1;",
        "  BAZ = 2;",
        "}",
        "message Foo {",
        "  int32 MyField2 = 1;",
        "  message Bar {",
        "    My" + CARET_TAG,
        "  }",
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "MyEnum", "MyMessage");
  }

  public void testNonQualifiedTypeServiceMethodParam() {
    setInput(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "}",
        "service Baz {",
        "  rpc Bar (My" + CARET_TAG,
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "}",
        "service Baz {",
        "  rpc Bar (MyMessage",
        "}");
  }

  public void testNonQualifiedTypeServiceMethodReturn() {
    setInput(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "}",
        "service Baz {",
        "  rpc Bar (MyMessage) returns (My" + CARET_TAG,
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message MyMessage {",
        "  int32 MyField1 = 1;",
        "}",
        "enum MyEnum {",
        "  FOO = 1;",
        "}",
        "service Baz {",
        "  rpc Bar (MyMessage) returns (MyMessage",
        "}");
  }

  public void testNoMessageEnumTypesInMapKeys() {
    setInput(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message MyEnum {",
        "  FOO = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  map<My" + CARET_TAG);
    assertNoCompletions();
  }

  public void testMessageTypesInMapValues() {
    setInput(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message MyEnum {",
        "  FOO = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  map<int32, My" + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "MyEnum", "MyMessage");
  }

  public void testFullyQualifiedType() {
    withPackage("foo.bar");
    setInput(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  .foo.bar.My" + CARET_TAG,
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  .foo.bar.MyMessage",
        "}");
  }

  public void testPartialPackageNameType() {
    withPackage("foo.bar");
    setInput(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  bar.My" + CARET_TAG,
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message MyMessage {",
        "  int32 F = 1;",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  bar.MyMessage",
        "}");
  }

  public void testPartiallyQualifiedType() {
    setInput(
        "message Bar {",
        "  message MyMessage {",
        "  }",
        "}",
        "message Foo {",
        "  message Bar {",
        "    message MyMessage {",
        "    }",
        "  }",
        "  int32 G = 1;",
        "  Bar.My" + CARET_TAG,
        "}");
    // Should have a unique choice, even though there's two Bar.Message, only the one within the
    // Foo scope should match.
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message Bar {",
        "  message MyMessage {",
        "  }",
        "}",
        "message Foo {",
        "  message Bar {",
        "    message MyMessage {",
        "    }",
        "  }",
        "  int32 G = 1;",
        "  Bar.MyMessage",
        "}");
  }

  public void testPartiallyQualifiedTypeNoMatchingChildren() {
    setInput(
        "message Bar {",
        "  message MyMessage {",
        "  }",
        "}",
        "message Foo {",
        "  message Bar {",
        "    message MyMessage {",
        "    }",
        "  }",
        "  int32 G = 1;",
        "  Bar.Zzz" + CARET_TAG,
        "}");
    assertNoCompletions();
  }

  public void testPartiallyQualifiedTypeNoMatchingQualifier() {
    setInput(
        "message Bar {",
        "  message MyMessage {",
        "  }",
        "}",
        "message Foo {",
        "  message Bar {",
        "    message MyMessage {",
        "    }",
        "  }",
        "  int32 G = 1;",
        "  Bazzz.My" + CARET_TAG,
        "}");
    assertNoCompletions();
  }

  public void testNoCompleteOutOfScope() {
    setInput(
        "message Bar {",
        "  message MyMessage {",
        "  }",
        "}",
        "message Foo {",
        "  int32 G = 1;",
        "  My" + CARET_TAG,
        "}");
    assertNoCompletions();
  }

  public void testNoBuiltinTypesInPartiallyQualifiedType() {
    setInput(
        "message Foo {",
        "  message Bar {",
        "    message Xyz {",
        "      bool a_field = 1;",
        "    }",
        "    message Abc {",
        "      bool b_field = 1;",
        "    }",
        "  }",
        "  Bar." + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "Abc", "Xyz");
  }

  public void testNoBuiltinTypesInFullyQualifiedType() {
    withPackage("com.bar.baz");
    setInput("message Foo {", "  ." + CARET_TAG);
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "com.");
  }

  public void testFuzzyComplete() {
    setInput(
        "message NotExpensiveMessage {",
        "  int32 pennies = 1;",
        "}",
        "message ExpensiveMessage {",
        "  repeated int32 dollars = 1;",
        "}",
        "message Test {",
        "  Exp" + CARET_TAG,
        "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "ExpensiveMessage", "NotExpensiveMessage");
  }

  public void testCompleteImportSamePackage() {
    setAdditionalFile("import_me.proto", "message Xyz {", "}", "message Xyzw {", "}");
    setInput("import \"import_me.proto\";", "message Test {", "  X" + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "Xyz", "Xyzw");
  }

  public void testNoCompleteWithoutQualifierImportOtherPackage() {
    withPackage("com.bar.baz");
    setAdditionalFile("import_me.proto", "message Xyz {", "}", "message Xyzw {", "}");
    withPackage("com.bar.foo");
    setInput("import \"import_me.proto\";", "message Test {", "  X" + CARET_TAG, "}");
    assertNoCompletions();
  }

  public void testCompleteWithQualifierImportOtherPackage() {
    withPackage("com.bar.baz");
    setAdditionalFile("import_me.proto", "message Xyz {", "}", "message Xyzw {", "}");
    withPackage("com.bar.foo");
    setInput("import \"import_me.proto\";", "message Test {", "  com.bar.baz.X" + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "Xyz", "Xyzw");
  }

  public void testCompletePackageUnique() {
    // Test de-duplicating different package symbol objects.
    withPackage("com.bar.baz");
    setAdditionalFile("import_me.proto", "message Xyz {", "}");
    withPackage("com.bar.foo");
    setInput("import \"import_me.proto\";", "message Test {", "  ." + CARET_TAG, "}");
    assertTrue(completeWithUniqueChoice());
    assertResult("import \"import_me.proto\";", "message Test {", "  .com.", "}");
  }

  public void testCompletePackageMessageClash() {
    // Test where a package and message have the same symbol.
    // We don't attempt to de-duplicate.
    withPackage("com.bar.baz");
    setAdditionalFile("import_me.proto", "message Xyz {", "}");
    withPackage("com");
    setInput("import \"import_me.proto\";", "message bar {", "  com." + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "bar.", "bar");
  }

  public void noCompleteMapEntry() { //todo
    // Test that the generated MapEntry message does not appear in completion suggestions.
    setInput("message Bar {", "  map<string,string> test_map = 1;", "  Tes" + CARET_TAG, "}");
    assertNoCompletions();
  }
}
