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

import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.lang.PbParserDefinition;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

@TestApplication
public class PbParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, TestUtils.getProtobufCoreTestDataPath() + "/lang/parser", "proto.testdata", new PbParserDefinition());

  /** Test against Unittest.proto from the official protoc release. */
  @Test
  public void testUnittest() {
    parsingFixture.get().doTest(true);
  }

  /** Test against unittest_proto3.proto from the official protoc release. */
  @Test
  public void testUnittestProto3() {
    parsingFixture.get().doTest(true);
  }

  /** Test against unittest_custom_options.proto from the official protoc release. */
  @Test
  public void testUnittestCustomOptions() {
    parsingFixture.get().doTest(true);
  }

  /** Test against Recovery.proto. */
  @Test
  public void testRecovery() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testUnittestExtensionRange() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testMapAsTypeName() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testMaxRanges() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testEnumReservedRange() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testEnumReservedRangeEditions() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testSymbolVisibility() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testImportOption() {
    parsingFixture.get().doTest(true);
  }
}
