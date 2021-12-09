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

import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

/** Test structure view. */
public class PbStructureViewTest extends PbCodeInsightFixtureTestCase {

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath() + "/ide/views/";
  }

  private void testStructureView(String expectedLines) {
    myFixture.testStructureView(
        viewComponent -> {
          // since IJ moved to an async structure tree model, expanded state isn't correctly set up
          // in integration tests. So run 'restoreState' to reset it.
          viewComponent.restoreState();
          assertTreeEqual(viewComponent.getTree(), expectedLines);
        });
  }

  public void testExtends() {
    myFixture.configureByFile(getTestDataPath() + "Extends.proto");
    String expectedLines =
        "-Extends.proto\n"
            + " -Foo\n"
            + "  field1\n"
            + "  field2\n"
            + "  extensions 100 to 199;\n"
            + " -extend Foo\n"
            + "  field126\n";
    testStructureView(expectedLines);
  }

  public void testNestedMessage() {
    myFixture.configureByFile(getTestDataPath() + "NestedMessage.proto");
    String expectedLines =
        "-NestedMessage.proto\n"
            + " -WithoutNest\n"
            + "  field1\n"
            + "  field2\n"
            + " -WithNest\n"
            + "  field1\n"
            + "  field2\n"
            + "  +NestedMessage\n"
            + "  +NestedEnum\n"
            + "  msg_instance\n"
            + "  enum_instance\n"
            + "  +Group\n"
            + "  +oneof_field\n";
    testStructureView(expectedLines);
  }

  public void testReservedFields() {
    myFixture.configureByFile(getTestDataPath() + "ReservedFields.proto");
    String expectedLines =
        "-ReservedFields.proto\n"
            + " -TestReservedFields\n"
            + "  field1\n"
            + "  reserved 2, 15, 9 to 11;\n"
            + "  reserved \"bar\", \"baz\";\n";
    testStructureView(expectedLines);
  }

  public void testServiceRpc() {
    myFixture.configureByFile(getTestDataPath() + "ServiceRpc.proto");
    String expectedLines =
        "-ServiceRpc.proto\n"
            + " FooRequest\n"
            + " FooResponse\n"
            + " -TestService\n"
            + "  Foo\n"
            + "  Bar\n"
            + " BarRequest\n"
            + " BarResponse\n";
    testStructureView(expectedLines);
  }
}
