// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSMultiFileFixture.Companion.jsMultiFileFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.ui.TestDialog
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.ThrowableRunnable
import org.angular2.Angular2TestUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

@TestApplication
class Angular2MoveTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsMultiFileFixture(
    projectFixture,
    JSFixtureOptions(Angular2TestUtil.getBaseTestDataPath() + "refactoring/move").withBasePath("/"),
  )

  @BeforeEach
  fun setUp() {
    Registry.get("ast.loading.filter").setValue(true, js.testRootDisposable)
  }

  @Throws(Exception::class)
  @AfterEach
  fun tearDown() {
    TestDialogManager.setTestDialog(TestDialog.DEFAULT)
  }

  @Test
  fun testSimpleDotFileRelative() {
    doMultiFileTest("dest", "component.ts")
  }

  @Test
  fun testDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  @Test
  fun testDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  @Test
  fun testBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  @Test
  fun testFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/test1/app1.component.ts")
  }

  @Test
  fun testBatchDotBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  @Test
  fun testBatchDotFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  @Test
  fun testBatchBaseRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  @Test
  fun testBatchFileRelative() {
    doMultiFileTest("src/app/dest",
                    "src/app/app.component.ts",
                    "src/app/app.component.html",
                    "src/app/dest/test2/app2.component.ts",
                    "src/app/dest/test2/app2.component.html",
                    "src/app/test1/app1.component.ts",
                    "src/app/test1/app1.component.html")
  }

  @Test
  fun testFolderDotBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  @Test
  fun testFolderDotFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  @Test
  fun testFolderBaseRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  @Test
  fun testFolderFileRelative() {
    doMultiFileTest(createMove("src/app/dest", "src/app/test1"),
                    createMove("src/app", "src/app/dest/test2"))
  }

  private fun createMove(destinationDir: String, vararg files: String): ThrowableRunnable<IOException> {
    return ThrowableRunnable {
      val moveProcessor = MoveFilesOrDirectoriesProcessor(
        js.project, files.map { name: String -> map2FileOrDir(name) }.toTypedArray(),
        js.fixture.getPsiManager().findDirectory(js.fixture.getTempDirFixture().findOrCreateDir(destinationDir))!!,
        true, false, false,
        null, null)
      moveProcessor.run()
    }
  }

  private fun doMultiFileTest(destinationDir: String, vararg files: String) {
    doMultiFileTest(createMove(destinationDir, *files))
  }

  private fun doMultiFileTest(vararg actions: ThrowableRunnable<out Exception>) {
    js.doTest { _, _ ->
      for (action in actions) {
        action.run()
      }
    }
  }

  private fun map2FileOrDir(name: String): PsiElement {
    val vf = js.fixture.getTempDirFixture().getFile(name) ?: error("Can't find $name")
    val psiFile = js.fixture.getPsiManager().findFile(vf)
    if (psiFile != null) {
      return psiFile
    }
    return js.fixture.getPsiManager().findDirectory(vf) ?: error("Can't find PsiDir or PsiFile for $name")
  }
}
