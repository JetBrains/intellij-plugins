package com.intellij.dts.documentation

import com.intellij.dts.DtsTestBase
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.platform.backend.documentation.impl.computeDocumentationAsync
import kotlinx.coroutines.awaitAll

abstract class DtsDocumentationTest : DtsTestBase() {
  override fun runInDispatchThread(): Boolean = false

  override fun runFromCoroutine(): Boolean = true

  protected fun doTest() {
    val content = getTestFixture("overlay")

    myFixture.configureByText("esp32.overlay", content)

    val documentations = runBlockingCancellable {
      val provider = IdeDocumentationTargetProvider.getInstance(project)

      val targets = readAction {
        provider.documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset - 1).map { it.createPointer() }
      }

      targets.map(::computeDocumentationAsync).awaitAll().mapNotNull { it?.html }
    }

    assertOneElement(documentations)
    compareWithTestFixture("html", documentations.first())
  }
}