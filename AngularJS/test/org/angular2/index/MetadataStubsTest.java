// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.angularjs.AngularTestUtil;

public class MetadataStubsTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testMetadataJsonFileType() {
    PsiFile file = myFixture.configureByFile("common.metadata.json");
    assert file != null;
    assert file.getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider;
    assert file.getViewProvider().getAllFiles().get(1) instanceof MetadataFileImpl;
  }

  public void testJsonFileType() {
    PsiFile file = myFixture.configureByFile("package.json");
    assert file != null;
    assert file.getViewProvider() instanceof SingleRootFileViewProvider;
  }
}
