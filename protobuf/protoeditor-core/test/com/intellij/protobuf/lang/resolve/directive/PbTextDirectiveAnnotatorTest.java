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
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.psi.PsiFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

/** Tests for comment-based format directives in text format files. */
public class PbTextDirectiveAnnotatorTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestUtils.addTestFileResolveProvider(getProject(), getTestRootDisposable());
    myFixture.configureByFile("lang/resolve/root_message.proto");
  }

  private void doTest(String filename) {
    PsiFile testFile = myFixture.configureByFile(filename);
    myFixture.testHighlighting(
        /* checkWarnings= */ true,
        /* checkInfos= */ false,
        /* checkWeakWarnings= */ true,
        testFile.getVirtualFile());
  }

  public void testInvalidFile() {
    doTest("lang/resolve/directive/InvalidFile.pb");
  }

  public void testInvalidMessage() {
    doTest("lang/resolve/directive/InvalidMessage.pb");
  }

  public void testMissingValues() {
    doTest("lang/resolve/directive/MissingValues.pb");
  }

  public void testFileWithoutMessage() {
    doTest("lang/resolve/directive/FileWithoutMessage.pb");
  }

  public void testMessageWithoutFile() {
    doTest("lang/resolve/directive/MessageWithoutFile.pb");
  }

  public void testNotAMessage() {
    doTest("lang/resolve/directive/NotAMessage.pb");
  }

  public void testImports() {
    myFixture.configureByFile("lang/options/any.proto");
    myFixture.configureByFile("lang/resolve/import_message.proto");
    myFixture.configureByFile("lang/resolve/import_root.proto");
    myFixture.configureByFile("lang/resolve/import_explicit1.proto");
    myFixture.configureByFile("lang/resolve/import_explicit2.proto");
    myFixture.configureByFile("lang/resolve/import_any.proto");
    doTest("lang/resolve/directive/Imports.pb");
  }
}
