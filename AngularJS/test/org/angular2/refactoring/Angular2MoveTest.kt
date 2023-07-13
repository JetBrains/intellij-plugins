// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.refactoring

import com.intellij.openapi.ui.TestDialog
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.psi.PsiElement
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor
import com.intellij.util.ThrowableRunnable
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angularjs.AngularTestUtil
import java.io.IOException

class Angular2MoveTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "refactoring/move"
  }

  override fun getTestRoot(): String {
    return "/"
  }

  @Throws(Exception::class)
  override fun tearDown() {
    try {
      TestDialogManager.setTestDialog(TestDialog.DEFAULT)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testSimpleDotFileRelative() {
    doMultiFileTest("dest", "component.ts")
  }

  fun testDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  fun testDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  fun testBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  fun testFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  fun testBatchDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  fun testBatchDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  fun testBatchBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  fun testBatchFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  fun testFolderDotBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  fun testFolderDotFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  fun testFolderBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  fun testFolderFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  private fun createMove(destinationDir: String, vararg files: String): ThrowableRunnable<IOException> {
    return ThrowableRunnable {
      val moveProcessor = MoveFilesOrDirectoriesProcessor(
        project, files.map { name: String -> map2FileOrDir(name) }.toTypedArray(),
        myFixture.getPsiManager().findDirectory(myFixture.getTempDirFixture().findOrCreateDir(destinationDir))!!,
        true, false, false,
        null, null)
      moveProcessor.run()
    }
  }

  private fun doMultiFileTest(destinationDir: String, vararg files: String) {
    doMultiFileTest(createMove(destinationDir, *files))
  }

  private fun doMultiFileTest(vararg actions: ThrowableRunnable<out Exception>) {
    doTest { _, _ ->
      for (action in actions) {
        action.run()
      }
    }
  }

  private fun map2FileOrDir(name: String): PsiElement {
    val vf = myFixture.getTempDirFixture().getFile(name) ?: error("Can't find $name")
    val psiFile = myFixture.getPsiManager().findFile(vf)
    if (psiFile != null) {
      return psiFile
    }
    return myFixture.getPsiManager().findDirectory(vf) ?: error("Can't find PsiDir or PsiFile for $name")
  }
}
