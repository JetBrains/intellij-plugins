// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angular2.lang.metadata.MetadataJsonFileViewProviderFactory;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.angularjs.AngularTestUtil;

public class MetadataStubsTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testMetadataJsonFileTypeBinary() {
    VirtualFile vFile = myFixture.copyFileToProject("common.metadata.json");
    myFixture.configureByFiles("package.json",  "common.d.ts");
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    assert file != null;
    assert file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider;
    assert file.getViewProvider().getAllFiles().get(0) instanceof MetadataFileImpl;
  }

  public void testMetadataJsonFileTypeNormal() {
    PsiFile file = myFixture.configureByFiles("common.metadata.json", "package.json")[0];
    assert file != null;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
    assert !(file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider);
    assert file.getViewProvider().getAllFiles().get(0) instanceof JsonFileImpl;
  }

  public void testJsonFileType() {
    PsiFile file = myFixture.configureByFile("package.json");
    assert file != null;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
    assert !(file.getViewProvider() instanceof MetadataJsonFileViewProviderFactory.MetadataFileViewProvider);
    assert file.getViewProvider().getAllFiles().get(0) instanceof JsonFileImpl;
  }
}
