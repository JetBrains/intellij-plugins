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
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.ide.editing.ProtoBraceMatcher;
import com.intellij.protobuf.lang.PbTextParserDefinition;
import com.intellij.testFramework.ParsingTestCase;

public class PbTextParserTest extends ParsingTestCase {

  public PbTextParserTest() {
    super("lang/parser", "pb", new PbTextParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    addExplicitExtension(LanguageBraceMatching.INSTANCE, myLanguage, new ProtoBraceMatcher());
  }

  @Override
  public String getTestDataPath() {
    return PathManagerEx.getHomePath(PbTextParserTest.class) + "/" + TestUtils.getTestdataPath();
  }

  /** Test against Simple.pb. */
  public void testSimple() {
    doTest(true);
  }

  /** Test against SpaceAfterNumber.pb. */
  public void testSpaceAfterNumber() {
    doTest(true);
  }
}
