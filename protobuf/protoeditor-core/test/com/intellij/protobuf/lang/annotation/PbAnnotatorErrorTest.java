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

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests for {@link PbAnnotator} error annotations. */
@TestApplication
public class PbAnnotatorErrorTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp(@TestDisposable Disposable disposable) throws Exception {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    myFixture.addFileToProject(
        TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
        myFixture.getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, disposable);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  private void doTest(String filename) {
    PsiFile testFile = myFixture.configureByFile(filename);
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testHighlighting(
        /* checkWarnings= */ false,
        /* checkInfos= */ false,
        /* checkWeakWarnings= */ false,
        testFile.getVirtualFile()));
  }

  @Test
  public void testTypeReferenceErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    myFixture.configureByFile("lang/annotation/import2.proto");
    doTest("lang/annotation/TypeReferenceErrors.proto.testdata");
  }

  @Test
  public void testImportReferenceErrorAnnotations() {
    doTest("lang/annotation/ImportReferenceErrors.proto.testdata");
  }

  @Test
  public void testOptionReferenceErrorAnnotations() {
    doTest("lang/annotation/OptionReferenceErrors.proto.testdata");
  }

  @Test
  public void testGroupOptionReferenceErrorAnnotations() {
    doTest("lang/annotation/GroupOptionReferenceErrors.proto.testdata");
  }

  @Test
  public void testStringErrorAnnotations() {
    doTest("lang/annotation/StringErrors.proto.testdata");
  }

  @Test
  public void testOptionValueErrors() {
    doTest("lang/annotation/OptionValueErrors.proto.testdata");
  }

  @Test
  public void testTypeValueErrors() {
    doTest("lang/annotation/TypeValueErrors.proto.testdata");
  }

  @Test
  public void testTextStringErrors() {
    doTest("lang/annotation/TextStringErrors.proto.testdata");
  }

  @Test
  public void testTextReferenceErrors() {
    myFixture.configureByFile("lang/options/any.proto");
    doTest("lang/annotation/TextReferenceErrors.proto.testdata");
  }

  @Test
  public void testTextValueErrors() {
    doTest("lang/annotation/TextValueErrors.proto.testdata");
  }

  @Test
  public void testOptionOccurrenceErrorAnnotations() {
    myFixture.configureByFile("lang/options/any.proto");
    doTest("lang/annotation/OptionOccurrenceErrors.proto.testdata");
  }

  @Test
  public void testIncorrectTypeErrorAnnotations() {
    doTest("lang/annotation/IncorrectTypeErrors.proto.testdata");
  }

  @Test
  public void testProto2ErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    doTest("lang/annotation/Proto2Errors.proto.testdata");
  }

  @Test
  public void testProto3ErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    myFixture.configureByFile("lang/annotation/proto2enum.proto");
    doTest("lang/annotation/Proto3Errors.proto.testdata");
  }

  @Test
  public void testProtoEditionUnsupportedErrorAnnotations() {
    doTest("lang/annotation/EditionUnsupportedErrors.proto.testdata");
  }

  @Test
  public void testProtoEdition2023ErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    doTest("lang/annotation/Edition2023Errors.proto.testdata");
  }

  @Test
  public void testProtoEdition2024ErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/import1.proto");
    doTest("lang/annotation/Edition2024Errors.proto.testdata");
  }
  
  @Test
  public void testFieldErrorAnnotations() {
    doTest("lang/annotation/FieldErrors.proto.testdata");
  }

  @Test
  public void testOptionScopingAnnotations() {
    myFixture.configureByFile("lang/annotation/scopingouter.proto");
    doTest("lang/annotation/OptionScoping.proto.testdata");
  }

  @Test
  public void testOptionMiscErrorAnnotations() {
    doTest("lang/annotation/OptionMiscErrors.proto.testdata");
  }

  @Test
  public void testEnumErrorAnnotations() {
    doTest("lang/annotation/EnumErrors.proto.testdata");
  }

  @Test
  public void testOneofErrorAnnotations() {
    doTest("lang/annotation/OneofErrors.proto.testdata");
  }

  @Test
  public void testExtendErrorAnnotations() {
    doTest("lang/annotation/ExtendErrors.proto.testdata");
  }

  @Test
  public void testNameConflictErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/conflictimport1.proto");
    myFixture.configureByFile("lang/annotation/conflictimport2.proto");
    doTest("lang/annotation/NameConflictErrors.proto.testdata");
  }

  @Test
  public void testNameConflictPackageReuseErrorAnnotations() {
    myFixture.configureByFile("lang/annotation/conflictimport1.proto");
    myFixture.configureByFile("lang/annotation/conflictimport2.proto");
    doTest("lang/annotation/NameConflictPackageReuse.proto.testdata");
  }

  @Test
  public void testMessageSetErrorAnnotations() {
    doTest("lang/annotation/MessageSetErrors.proto.testdata");
  }

  @Test
  public void testDuplicatePackageStatementAnnotation() {
    doTest("lang/annotation/DuplicatePackageStatement.proto.testdata");
  }

  @Test
  public void testReservedFieldErrorAnnotations() {
    doTest("lang/annotation/ReservedFieldErrors.proto.testdata");
  }
}
