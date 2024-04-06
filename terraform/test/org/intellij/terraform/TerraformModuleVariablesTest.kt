package org.intellij.terraform

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.components.service
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.intellij.terraform.config.inspection.HCLBlockMissingPropertyInspection
import org.intellij.terraform.config.inspection.TFDuplicatedVariableInspection
import org.intellij.terraform.config.inspection.TFVARSIncorrectElementInspection
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.local.LocalSchemaService

class TerraformModuleVariablesTest() : TerraformModuleVariablesTestBase("terraform/variables/tf-modules-subdirs")

class TerraformModuleVariablesUninitialisedTest() : TerraformModuleVariablesTestBase("terraform/variables/tf-modules-subdirs-uninitialised")

abstract class TerraformModuleVariablesTestBase(private val testDataRoot: String) : BasePlatformTestCase() {

  override fun getTestDataPath(): String? = TerraformTestUtils.getTestDataPath()

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    TypeModelProvider.globalModel // ensure loaded, to avoid falling on the timeout
    myFixture.enableInspections(
      TFVARSIncorrectElementInspection::class.java,
      TFDuplicatedVariableInspection::class.java,
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