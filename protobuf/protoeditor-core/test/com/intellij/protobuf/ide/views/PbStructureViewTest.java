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
package com.intellij.protobuf.ide.views;

import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.testFramework.PlatformTestUtil;

import javax.swing.*;


/** Test structure view. */
public class PbStructureViewTest extends PbCodeInsightFixtureTestCase {

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath() + "/ide/views/";
  }

  private void testStructureView(String expectedLines) {
    myFixture.testStructureView(it -> {
      JTree tree = it.getTree();
      PlatformTestUtil.waitWhileBusy(tree);
      PlatformTestUtil.expandAll(tree);
      PlatformTestUtil.assertTreeEqual(tree, expectedLines);
    });
  }

  public void testExtends() {
    myFixture.configureByFile(getTestDataPath() + "Extends.proto");
    String expectedLines =
      """
        -Extends.proto
         -Foo
          field1
          field2
          extensions 100 to 199;
         -extend Foo
          field126
        """;
    testStructureView(expectedLines);
  }

  public void testNestedMessage() {
    myFixture.configureByFile(getTestDataPath() + "NestedMessage.proto");
    String expectedLines =
      """
        -NestedMessage.proto
         -WithoutNest
          field1
          field2
         -WithNest
          field1
          field2
          -NestedMessage
           -NestedMessage2
            inner_nested1
           nested1
           nested2
           inner_nested_instance
          -NestedEnum
           VARIANT1
           VARIANT2
          msg_instance
          enum_instance
          -Group
           field1
           field2
          -oneof_field
           oneof_uint32
           oneof_nested_message
           oneof_string
           oneof_bytes
        """;
    testStructureView(expectedLines);
  }

  public void testReservedFields() {
    myFixture.configureByFile(getTestDataPath() + "ReservedFields.proto");
    String expectedLines =
      """
        -ReservedFields.proto
         -TestReservedFields
          field1
          reserved 2, 15, 9 to 11;
          reserved "bar", "baz";
        """;
    testStructureView(expectedLines);
  }

  public void testServiceRpc() {
    myFixture.configureByFile(getTestDataPath() + "ServiceRpc.proto");
    String expectedLines =
      """
        -ServiceRpc.proto
         FooRequest
         FooResponse
         -TestService
          Foo
          Bar
         BarRequest
         BarResponse
        """;
    testStructureView(expectedLines);
  }
}
