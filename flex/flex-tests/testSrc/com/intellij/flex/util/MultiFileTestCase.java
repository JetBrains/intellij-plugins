// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.PsiTestUtil;

import java.io.File;
import java.io.IOException;

public abstract class MultiFileTestCase extends JSDaemonAnalyzerTestCase {
  protected boolean myDoCompare = true;

  protected void doTest(final PerformAction performAction) {
    doTest(performAction, getTestName(true));
  }

  protected void doTest(final PerformAction performAction, final boolean lowercaseFirstLetter) {
    doTest(performAction, getTestName(lowercaseFirstLetter));
  }

  protected void doTest(final PerformAction performAction, final String testName) {
    try {
      String path = getTestDataPath() + getBasePath() + testName;

      String pathBefore = path + "/before";
      VirtualFile rootDir = createTestProjectStructure(pathBefore, false);
      prepareProject(rootDir);
      PsiDocumentManager.getInstance(myProject).commitAllDocuments();

      String pathAfter = path + "/after";
      final VirtualFile rootAfter = LocalFileSystem.getInstance().findFileByPath(pathAfter.replace(File.separatorChar, '/'));
      IndexingTestUtil.waitUntilIndexesAreReady(getProject());
      performAction.performAction(rootDir, rootAfter);
      WriteCommandAction.runWriteCommandAction(getProject(),
                                               () -> PostprocessReformattingAspect.getInstance(myProject).doPostponedFormatting());

      FileDocumentManager.getInstance().saveAllDocuments();

      if (myDoCompare) {
        compareResults(rootAfter, rootDir);
      }
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void compareResults(VirtualFile rootAfter, VirtualFile rootDir) throws IOException {
    PlatformTestUtil.assertDirectoriesEqual(rootAfter, rootDir);
  }

  protected void prepareProject(VirtualFile rootDir) {
    PsiTestUtil.addSourceContentToRoots(myModule, rootDir);
  }

  protected interface PerformAction {
    void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception;
  }
}