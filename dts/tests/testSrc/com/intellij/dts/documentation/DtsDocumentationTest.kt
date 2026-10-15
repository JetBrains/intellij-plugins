package com.intellij.dts.documentation

import com.intellij.dts.dtsFixture
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openapi.application.readAction
import com.intellij.platform.backend.documentation.impl.computeDocumentationAsync
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.assertj.core.api.Assertions.assertThat

abstract class DtsDocumentationTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  protected val dts by dtsFixture(projectFixture)

  protected val myFixture get() = dts.codeInsight

  protected open suspend fun doTest() {
    val content = dts.getTestFixture("overlay")

    myFixture.configureByText("esp32.overlay", content)

    val documentations = coroutineScope {
      val provider = IdeDocumentationTargetProvider.getInstance(dts.project)

      val targets = readAction {
        provider.documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset - 1).map { it.createPointer() }
      }

      targets.map(::computeDocumentationAsync).awaitAll().mapNotNull { it?.html }
    }

    assertThat(documentations).hasSize(1)
    dts.compareWithTestFixture("html", documentations.first())
  }
}