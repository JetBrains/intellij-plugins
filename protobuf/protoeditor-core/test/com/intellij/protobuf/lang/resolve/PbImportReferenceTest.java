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
package com.intellij.protobuf.lang.resolve;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.VfsTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link PbImportReference}. */
@TestApplication
public class PbImportReferenceTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo, @TestDisposable Disposable disposable) throws Exception {
    myFixture = codeInsightFixture.get();
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), false);
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    myFixture.addFileToProject(
            TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
            myFixture.getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, disposable);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  @Test
  public void testImportSibling() {
    String siblingProto = "lang/resolve/Sibling.proto";

    final VirtualFile vFile =
        VfsTestUtil.findFileByCaseSensitivePath(myFixture.getTestDataPath() + siblingProto);
    assertNotNull(vFile);
    myFixture.copyFileToProject(vFile.getPath(), siblingProto);

    assertIsFileType(resolve(), siblingProto);
  }

  @Test
  public void testImportIncompleteQuotes() throws Exception {
    // Just test that there's no exception while attempting to resolve.
    String filePath = "lang/resolve/" + testName + ".proto.testdata";

    VirtualFile vFile = VfsTestUtil.findFileByCaseSensitivePath(myFixture.getTestDataPath() + filePath);
    assertNotNull(vFile, "file " + filePath + " not found");

    String fileText = StringUtil.convertLineSeparators(VfsUtilCore.loadText(vFile));
    int offset = fileText.indexOf("<caret>");
    assertTrue(offset >= 0);

    myFixture.copyFileToProject(vFile.getPath(), filePath);
    fileText = fileText.substring(0, offset) + fileText.substring(offset + "<caret>".length());

    myFixture.configureByText(PbFileType.INSTANCE, fileText);
    PsiReference ref = ReadAction.computeBlocking(() -> myFixture.getFile().findReferenceAt(offset));
    assertNull(ref);
  }

  private PsiElement resolve() {
    String filename = "lang/resolve/" + testName + ".proto.testdata";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertNotNull(ref);
    return ReadAction.computeBlocking(() -> ref.resolve());
  }

  private static void assertIsFileType(PsiElement target, String expectedPath) {
    assertInstanceOf(PbFile.class, target);
    String fileName = ((PbFile) target).getName();
    assertNotNull(fileName);
    assertEquals(new File(expectedPath).getName(), fileName);
  }
}
