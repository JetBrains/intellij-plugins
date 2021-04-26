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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.VfsTestUtil;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import org.junit.Assert;

import java.io.File;

/** Tests for {@link PbImportReference}. */
public class PbImportReferenceTest extends PbCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.addFileToProject(
            TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
            getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, getTestRootDisposable());
    TestUtils.registerTestdataFileExtension();
  }

  public void testImportSibling() {
    String siblingProto = "lang/resolve/Sibling.proto";

    final VirtualFile vFile =
        VfsTestUtil.findFileByCaseSensitivePath(getTestDataPath() + siblingProto);
    assertNotNull(vFile);
    myFixture.copyFileToProject(vFile.getPath(), siblingProto);

    assertIsFileType(resolve(), siblingProto);
  }

  public void testImportIncompleteQuotes() throws Exception {
    // Just test that there's no exception while attempting to resolve.
    String filePath = "lang/resolve/" + getTestName(false) + ".proto.testdata";

    VirtualFile vFile = VfsTestUtil.findFileByCaseSensitivePath(getTestDataPath() + filePath);
    assertNotNull("file " + filePath + " not found", vFile);

    String fileText = StringUtil.convertLineSeparators(VfsUtilCore.loadText(vFile));
    int offset = fileText.indexOf("<caret>");
    assertTrue(offset >= 0);

    myFixture.copyFileToProject(vFile.getPath(), filePath);
    fileText = fileText.substring(0, offset) + fileText.substring(offset + "<caret>".length());

    myFixture.configureByText(PbFileType.INSTANCE, fileText);
    PsiReference ref = getFile().findReferenceAt(offset);
    assertNull(ref);
  }

  private PsiElement resolve() {
    String filename = "lang/resolve/" + getTestName(false) + ".proto.testdata";
    PsiReference ref = myFixture.getReferenceAtCaretPosition(filename);
    assertNotNull(ref);
    return ref.resolve();
  }

  private static void assertIsFileType(PsiElement target, String expectedPath) {
    Assert.assertTrue(target instanceof PbFile);
    String fileName = ((PbFile) target).getName();
    Assert.assertNotNull(fileName);
    Assert.assertEquals(new File(expectedPath).getName(), fileName);
  }
}
