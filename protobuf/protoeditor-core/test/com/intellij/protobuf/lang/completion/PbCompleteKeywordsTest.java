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

import java.util.Collection;
import java.util.List;

import static com.intellij.testFramework.EditorTestUtil.CARET_TAG;

/** Tests for various keywords (message, enum) completion (see {@link PbCompletionContributor}). */
public class PbCompleteKeywordsTest extends PbCompletionContributorTestCase {

  public void testMessageAtTopLevel() {
    setInput("mes" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message ");
  }

  public void testNoMessageAsMessageIdentifier() {
    setInput("message mes" + CARET_TAG);
    assertNoCompletions();
  }

  public void testEnumAtTopLevel() {
    setInput("enu" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("enum ");
  }

  public void testServiceAtTopLevel() {
    setInput("ser" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("service ");
  }

  public void testExtendAtTopLevel() {
    setInput("ext" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("extend ");
  }

  public void testOptionAtTopLevel() {
    setInput("opt" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("option ");
  }

  public void testImportAtTopLevel() {
    setInput("imp" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("import ");
  }

  public void testMessageInMessageAutoInsertSpace() {
    setInput("message Foo {", "  mes" + CARET_TAG, "}");
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  message ", "}");
  }

  public void testNoMessageAsMessageIdentifierInMessage() {
    setInput("message Foo {", "  message mes" + CARET_TAG, "}");
    assertNoCompletions();
  }

  public void testNoMessageAsFieldIdentifier() {
    setInput("message Foo {", "  int32 mes" + CARET_TAG, "}");
    assertNoCompletions();
  }

  public void testNoMessageInEnum() {
    setInput("enum Foo {", "  mes" + CARET_TAG, "}");
    assertNoCompletions();
  }

  public void testGroupInProto2() {
    withSyntax("proto2");
    setInput("message Foo {", "  repeated gro" + CARET_TAG);
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  repeated group ");
  }

  public void testNoGroupInProto3() {
    withSyntax("proto3");
    setInput("message Foo {", "  repeated gro" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoGroupAsGroupIdentifier() {
    withSyntax("proto2");
    setInput("message Foo {", "  repeated group gro" + CARET_TAG);
    assertNoCompletions();
  }

  public void testNoGroupAsFullyQualifiedType() {
    withSyntax("proto2");
    withPackage("com.bar");
    setInput("message Foo {", "  optional ." + CARET_TAG, "}");
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  optional .com.", "}");
  }

  public void testNoMessageInExtend() {
    setInput(
        "message Foo {",
        "  int32 field123 = 123;",
        "  extensions 10000 to max;",
        "}",
        "extend Foo {",
        "  mes" + CARET_TAG,
        "}");
    assertNoCompletions();
  }

  public void testGroupInExtend() {
    withSyntax("proto2");
    setInput(
        "message Foo {",
        "  optional int32 field123 = 123;",
        "  extensions 10000 to max;",
        "}",
        "extend Foo {",
        "  optional gro" + CARET_TAG,
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message Foo {",
        "  optional int32 field123 = 123;",
        "  extensions 10000 to max;",
        "}",
        "extend Foo {",
        "  optional group ",
        "}");
  }

  public void testMessageWithinExtendInGroup() {
    withSyntax("proto2");
    setInput(
        "message Foo {",
        "  extensions 10000 to max;",
        "}",
        "extend Foo {",
        "  optional group a_group_field = 1 {",
        "    mes" + CARET_TAG,
        "  }",
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message Foo {",
        "  extensions 10000 to max;",
        "}",
        "extend Foo {",
        "  optional group a_group_field = 1 {",
        "    message ",
        "  }",
        "}");
  }

  public void testExtendExtensionsInMessage() {
    setInput("message Foo {", "  ext" + CARET_TAG, "}");
    List<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "extend", "extensions");
  }

  public void testMapInMessageNoAutoInsertSpace() {
    setInput("message Foo {", "  ma" + CARET_TAG, "}");
    assertTrue(completeWithUniqueChoice());
    assertResult("message Foo {", "  map", "}");
  }

  public void testMessageInGroup() {
    withSyntax("proto2");
    setInput(
        "message Foo {",
        "  optional int32 field1 = 1;",
        "  optional string field2 = 2;",
        "  optional group Bar = 3 {",
        "    mes" + CARET_TAG,
        "  }",
        "}");
    assertTrue(completeWithUniqueChoice());
    assertResult(
        "message Foo {",
        "  optional int32 field1 = 1;",
        "  optional string field2 = 2;",
        "  optional group Bar = 3 {",
        "    message ",
        "  }",
        "}");
  }

  public void testOptionOrOptionalForProto2() {
    withSyntax("proto2");
    setInput("message Foo {", "  repeated int32 field1 = 1;", "  opt" + CARET_TAG, "}");
    Collection<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "option", "optional");
  }

  public void testOptionOrOptionalForProto3() {
    withSyntax("proto3");
    setInput("message Foo {", "  repeated int32 field1 = 1;", "  opt" + CARET_TAG, "}");
    Collection<String> completions = getCompletionItemsAsStrings();
    assertNotNull(completions);
    assertSameElements(completions, "option", "optional");
  }
}
