package org.intellij.terraform

import com.intellij.codeInsight.codeVision.CodeVisionHost
import com.intellij.codeInsight.codeVision.ui.model.CodeVisionListData
import com.intellij.openapi.application.*
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.blockingContextToIndicator
import com.intellij.testFramework.TestModeFlags
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection
import org.intellij.terraform.config.inspection.TfDuplicatedVariableInspection
import org.intellij.terraform.config.inspection.TfVARSIncorrectElementInspection
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.local.LocalSchemaService

class TfModuleVariablesTest() : TfModuleVariablesTestBase("terraform/variables/tf-modules-subdirs") {

  fun testModuleVariableDeclarationRename() = timeoutRunBlocking {
    val file = myFixture.configureByFile("modules/fake_module/variables.tf")
    val varToRename = file.text.indexOf("fake_var_1")
    writeAction {
      myFixture.editor.caretModel.moveToOffset(varToRename + 1)
    }
    val findUsages = readAction {
      blockingContextToIndicator {
        val elementAtCaret = myFixture.elementAtCaret
        myFixture.findUsages(elementAtCaret)
      }
    }

    assertEquals(1, findUsages.size)
    assertContainsElements(myFixture.getCodeVisionsForCaret(), "1 usage")
    writeAction { myFixture.renameElementAtCaret("new_fake_var_1") }
    myFixture.checkResult("main.tf", """
      module "main" {
        source = "./modules/fake_module"

        new_fake_var_1 = ""
        fake_var_2 = ""

        fake_var_4 = ""
      }
      
      module "this" {
        source    = "cloudposse/label/null"
        version   = "0.25.0"
        namespace = "em"
        stage     = "test-stage"
        name      = "test-name"
        delimiter = "-"
      }
      
    """.trimIndent(), true)
  }

}

internal suspend fun CodeInsightTestFixture.getCodeVisionsForCaret(): List<String> {
  TestModeFlags.set(CodeVisionHost.isCodeVisionTestKey, true, testRootDisposable)
  val visionHost = project.service<CodeVisionHost>()
  doHighlighting()
  withContext(Dispatchers.EDT) {
    writeIntentReadAction {
      visionHost.calculateCodeVisionSync(editor, testRootDisposable)
    }
  }

  return readAction {
    val doc = this.editor.document
    val lineNumber = doc.getLineNumber(this.editor.caretModel.offset)
    val inlays = this.editor.inlayModel.getAfterLineEndElementsForLogicalLine(lineNumber)
    inlays.flatMap {
      it.getUserData(CodeVisionListData.KEY)?.visibleLens?.map { it.longPresentation } ?: emptyList()
    }
  }
}


class TfModuleVariablesUninitialisedTest() : TfModuleVariablesTestBase("terraform/variables/tf-modules-subdirs-uninitialised")

abstract class TfModuleVariablesTestBase(private val testDataRoot: String) : BasePlatformTestCase() {

  override fun getTestDataPath(): String? = TfTestUtils.getTestDataPath()

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    TypeModelProvider.globalModel // ensure loaded, to avoid falling on the timeout
    myFixture.enableInspections(
      TfVARSIncorrectElementInspection::class.java,
      TfDuplicatedVariableInspection::class.java,
      HCLBlockMissingPropertyInspection::class.java,
    )
    runWriteActionAndWait {
      myFixture.copyDirectoryToProject(testDataRoot, ".")
    }
    timeoutRunBlocking {
      project.service<LocalSchemaService>().awaitModelsReady()
    }
  }

  fun testMissingVariables() {
    myFixture.testHighlighting("main.tf")
  }

  fun testMainVariables() {
    myFixture.testHighlighting("variables.tf")
  }

  fun testDuplicateInModule() {
    myFixture.testHighlighting("modules/fake_module/variables.tf")
  }

}