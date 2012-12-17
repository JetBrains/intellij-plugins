package com.jetbrains.lang.dart;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author: Fedor.Korotkov
 */
public class DartMoveTest extends MultiFileTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/");
  }

  @Override
  protected String getTestRoot() {
    return "/move/";
  }

  //Both names are relative to root directory
  private void doTest(String toMove, final String targetDirName) throws Exception {
    doTest(new String[]{toMove}, targetDirName);
  }

  //Both names are relative to root directory
  private void doTest(final String[] toMove, final String targetDirName) throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        Collection<PsiElement> files = new ArrayList<PsiElement>();
        for (String s : toMove) {
          final VirtualFile child = VfsUtil.findRelativeFile(s, rootDir);
          assertNotNull("Neither class nor file " + s + " not found", child);
          PsiElement file = myPsiManager.findFile(child);
          if (file == null) file = JavaPsiFacade.getInstance(myProject).findPackage(s);
          files.add(file);
        }
        final VirtualFile child1 = VfsUtil.findRelativeFile(targetDirName, rootDir);
        assertNotNull("Target dir " + targetDirName + " not found", child1);
        final PsiDirectory targetDirectory = myPsiManager.findDirectory(child1);
        assertNotNull(targetDirectory);

        new MoveFilesOrDirectoriesProcessor(myProject, PsiUtilCore.toPsiElementArray(files), targetDirectory,
                                            false, true, null, null).run();
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });
  }

  public void testMoveFile1() throws Exception {
    doTest("Foo.dart", "bar");
  }

  public void testMoveFile2() throws Exception {
    doTest("bar/Foo.dart", "");
  }
}
