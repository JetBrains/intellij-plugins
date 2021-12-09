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
package com.intellij.protobuf.lang.parser;

import com.intellij.lang.LanguageBraceMatching;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.ide.editing.ProtoBraceMatcher;
import com.intellij.protobuf.lang.PbParserDefinition;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.protobuf.lang.PbTextParserDefinition;
import com.intellij.testFramework.ParsingTestCase;

public class PbParserTest extends ParsingTestCase {

  public PbParserTest() {
    super("lang/parser", "proto.testdata", new PbParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // GeneratedParserUtilBase uses brace matchers to assist in recovery.
    addExplicitExtension(LanguageBraceMatching.INSTANCE, myLanguage, new ProtoBraceMatcher());

    // Needed because proto files directly include prototext elements.
    addExplicitExtension(
        LanguageParserDefinitions.INSTANCE, PbTextLanguage.INSTANCE, new PbTextParserDefinition());
  }

  @Override
  public String getTestDataPath() {
    return PathManagerEx.getHomePath(PbParserTest.class) + "/" + TestUtils.getTestdataPath();
  }

  /** Test against Unittest.proto from the official protoc release. */
  public void testUnittest() {
    doTest(true);
  }

  /** Test against unittest_proto3.proto from the official protoc release. */
  public void testUnittestProto3() {
    doTest(true);
  }

  /** Test against unittest_custom_options.proto from the official protoc release. */
  public void testUnittestCustomOptions() {
    doTest(true);
  }

  /** Test against Recovery.proto. */
  public void testRecovery() {
    doTest(true);
  }

  public void testUnittestExtensionRange() {
    doTest(true);
  }

  public void testMapAsTypeName() {
    doTest(true);
  }

  public void testMaxRanges() {
    doTest(true);
  }

  public void testEnumReservedRange() {
    doTest(true);
  }
}
