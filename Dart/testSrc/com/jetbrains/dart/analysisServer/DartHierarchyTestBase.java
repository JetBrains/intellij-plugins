// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.codeInsight.hierarchy.HierarchyViewTestFixture;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.util.DartTestUtils;

import java.io.File;
import java.io.IOException;

public abstract class DartHierarchyTestBase extends CodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  protected void doHierarchyTest(final Computable<? extends HierarchyTreeStructure> treeStructureComputable,
                                 final String... fileNames) throws IOException {
    myFixture.configureByFiles(fileNames);
    myFixture.doHighlighting(); // warm up

    final String verificationFilePath = myFixture.getTestDataPath() + "/" + getTestName(false) + "_verification.xml";
    HierarchyTreeStructure structure = treeStructureComputable.compute();
    File file = new File(verificationFilePath);
    if (!file.exists()) {
      FileUtil.writeToFile(file, HierarchyViewTestFixture.dump(structure, null, null, 0));
      throw new IllegalStateException("File: " + file.getPath() + " doesn't exist. A new file created.");
    }
    HierarchyViewTestFixture.doHierarchyTest(structure, file);
  }
}
