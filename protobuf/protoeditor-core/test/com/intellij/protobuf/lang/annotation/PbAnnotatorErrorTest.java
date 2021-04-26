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
package com.intellij.protobuf.lang.annotation;

import com.intellij.psi.PsiFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

/** Tests for {@link PbAnnotator} error annotations. */
public class PbAnnotatorErrorTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.addFileToProject(
        TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
        getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  private void doTest(String filename) {
    PsiFile testFile = myFixture.configureByFile(filename);
    myFixture.testHighlighting(
        /* checkWarnings= */ false,
        /* checkInfos= */ false,
        /* checkWeakWarnings= */ false,
        testFile.getVirtualFile());
  }

  public void testTypeReferenceErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    myFixture.configureByFile("lang/annotation/import2.proto");
    doTest("lang/annotation/TypeReferenceErrors.proto.testdata");
  }

  public void testImportReferenceErrorAnnotations() {
    doTest("lang/annotation/ImportReferenceErrors.proto.testdata");
  }

  public void testOptionReferenceErrorAnnotations() {
    doTest("lang/annotation/OptionReferenceErrors.proto.testdata");
  }

  public void testGroupOptionReferenceErrorAnnotations() {
    doTest("lang/annotation/GroupOptionReferenceErrors.proto.testdata");
  }

  public void testStringErrorAnnotations() {
    doTest("lang/annotation/StringErrors.proto.testdata");
  }

  public void testOptionValueErrors() {
    doTest("lang/annotation/OptionValueErrors.proto.testdata");
  }

  public void testTypeValueErrors() {
    doTest("lang/annotation/TypeValueErrors.proto.testdata");
  }

  public void testTextStringErrors() {
    doTest("lang/annotation/TextStringErrors.proto.testdata");
  }

  public void testTextReferenceErrors() {
    myFixture.configureByFile("lang/options/any.proto");
    doTest("lang/annotation/TextReferenceErrors.proto.testdata");
  }

  public void testTextValueErrors() {
    doTest("lang/annotation/TextValueErrors.proto.testdata");
  }

  public void testOptionOccurrenceErrorAnnotations() {
    myFixture.configureByFile("lang/options/any.proto");
    doTest("lang/annotation/OptionOccurrenceErrors.proto.testdata");
  }

  public void testIncorrectTypeErrorAnnotations() {
    doTest("lang/annotation/IncorrectTypeErrors.proto.testdata");
  }

  public void testProto2ErrorAnnotations() {
    doTest("lang/annotation/Proto2Errors.proto.testdata");
  }

  public void testProto3ErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    myFixture.configureByFile("lang/annotation/proto2enum.proto");
    doTest("lang/annotation/Proto3Errors.proto.testdata");
  }

  public void testFieldErrorAnnotations() {
    doTest("lang/annotation/FieldErrors.proto.testdata");
  }

  public void testOptionScopingAnnotations() {
    myFixture.configureByFile("lang/annotation/scopingouter.proto");
    doTest("lang/annotation/OptionScoping.proto.testdata");
  }

  public void testOptionMiscErrorAnnotations() {
    doTest("lang/annotation/OptionMiscErrors.proto.testdata");
  }

  public void testEnumErrorAnnotations() {
    doTest("lang/annotation/EnumErrors.proto.testdata");
  }

  public void testOneofErrorAnnotations() {
    doTest("lang/annotation/OneofErrors.proto.testdata");
  }

  public void testExtendErrorAnnotations() {
    doTest("lang/annotation/ExtendErrors.proto.testdata");
  }

  public void testNameConflictErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/conflictimport1.proto");
    myFixture.configureByFile("lang/annotation/conflictimport2.proto");
    doTest("lang/annotation/NameConflictErrors.proto.testdata");
  }

  public void testNameConflictPackageReuseErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/conflictimport1.proto");
    myFixture.configureByFile("lang/annotation/conflictimport2.proto");
    doTest("lang/annotation/NameConflictPackageReuse.proto.testdata");
  }

  public void testMessageSetErrorAnnotations() {
    doTest("lang/annotation/MessageSetErrors.proto.testdata");
  }

  public void testDuplicatePackageStatementAnnotation() {
    doTest("lang/annotation/DuplicatePackageStatement.proto.testdata");
  }

  public void testReservedFieldErrorAnnotations() {
    doTest("lang/annotation/ReservedFieldErrors.proto.testdata");
  }
}
