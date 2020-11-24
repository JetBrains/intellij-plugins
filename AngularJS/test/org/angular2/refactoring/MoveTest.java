// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.ui.TestDialogManager;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor;
import com.intellij.util.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2MultiFileFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MoveTest extends Angular2MultiFileFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "move";
  }

  @NotNull
  @Override
  protected String getTestRoot() {
    return "/";
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      TestDialogManager.setTestDialog(TestDialog.DEFAULT);
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  public void testSimpleDotFileRelative() {
    doMultiFileTest("dest", "component.ts");
  }

  public void testDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts");
  }

  public void testDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts");
  }

  public void testBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts");
  }

  public void testFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts");
  }

  public void testBatchDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html");
  }

  public void testBatchDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html");
  }

  public void testBatchBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html");
  }

  public void testBatchFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html");
  }

  public void testFolderDotBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"));
  }

  public void testFolderDotFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"));
  }

  public void testFolderBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"));
  }

  public void testFolderFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"));
  }

  private ThrowableRunnable<IOException> createMove(String destinationDir, String... files) {
    return () -> {
      MoveFilesOrDirectoriesProcessor moveProcessor = new MoveFilesOrDirectoriesProcessor(
        getProject(), ContainerUtil.map2Array(files, PsiElement.class, this::map2FileOrDir),
        myFixture.getPsiManager().findDirectory(myFixture.getTempDirFixture().findOrCreateDir(destinationDir)),
        true, false, false,
        null, null);
      moveProcessor.run();
    };
  }

  private void doMultiFileTest(String destinationDir, String... files) {
    doMultiFileTest(createMove(destinationDir, files));
  }

  private void doMultiFileTest(ThrowableRunnable<? extends Exception>... actions) {
    doTest((rootDir, rootAfter) -> {
      for (ThrowableRunnable<? extends Exception> action : actions) {
        action.run();
      }
    });
  }

  @NotNull
  private PsiElement map2FileOrDir(String name) {
    VirtualFile vf = myFixture.getTempDirFixture().getFile(name);
    assert vf != null : "Can't find " + name;
    PsiFile psiFile = myFixture.getPsiManager().findFile(vf);
    if (psiFile != null) {
      return psiFile;
    }
    PsiDirectory psiDirectory = myFixture.getPsiManager().findDirectory(vf);
    assert psiDirectory != null : "Can't find PsiDir or PsiFile for " + name;
    return psiDirectory;
  }

  public static class BranchTest extends MoveTest {
    @Override
    protected void setUp() throws Exception {
      super.setUp();
      Registry.get("run.refactorings.in.model.branch").setValue(true, getTestRootDisposable());
    }
  }
}
