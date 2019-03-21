// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestFixture;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.io.File;
import java.io.IOException;

public abstract class DartHierarchyTestBase extends CodeInsightFixtureTestCase {
  private final HierarchyViewTestFixture myHierarchyViewTestFixture = new HierarchyViewTestFixture();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance(getProject()).serverReadyForRequest();
  }

  protected void doHierarchyTest(final Computable<? extends HierarchyTreeStructure> treeStructureComputable,
                                 final String... fileNames) throws IOException {
    PsiFile[] files = myFixture.configureByFiles(fileNames);
    ensureNavigationInfoAvailable(files);

    final String verificationFilePath = myFixture.getTestDataPath() + "/" + getTestName(false) + "_verification.xml";
    HierarchyTreeStructure structure = treeStructureComputable.compute();
    File file = new File(verificationFilePath);
    if (!file.exists()) {
      FileUtil.writeToFile(file, HierarchyViewTestFixture.dump(structure, null, 0));
      throw new IllegalStateException("File: " + file.getPath() + " doesn't exist. A new file created.");
    }
    myHierarchyViewTestFixture.doHierarchyTest(structure, FileUtil.loadFile(file));
  }

  private void ensureNavigationInfoAvailable(PsiFile[] files) {
    for (PsiFile file : files) {
      DartAnalysisServerService.getInstance(getProject()).analysis_getNavigation(file.getVirtualFile(), 0, file.getTextLength());
    }
  }
}
