package org.intellij.terraform.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock

internal class TfTestReferenceTest : BasePlatformTestCase() {
  val fileName: String = "base.$TF_TEST_EXTENSION"

  fun testVariablesReference() {
    myFixture.addFileToProject("variables.tf", """
      variable "region" {
        type = string
      }
      variable "instance_count" {
        type = number
      }
    """.trimIndent())

    myFixture.configureByText(fileName, """
      variables {
        <caret>region = "us-east-1"
        instance_count = 3
      }
    """.trimIndent())

    val variable = myFixture.getReferenceAtCaretPosition()?.resolve() as? HCLBlock
                   ?: throw AssertionError("Expected variable block")
    assertEquals("region", variable.name)
    assertTrue(TfPsiPatterns.VariableRootBlock.accepts(variable))
  }

  fun testVariablesReferenceBasedOnModuleSource() {
    myFixture.addFileToProject("credentials/variables.tf", """
      variable "access_key" {
        type = string
      }
    """.trimIndent())

    myFixture.configureByText(fileName, """
      run "credentials" {
        module {
          source = "./credentials"
        }
        variables {
          <caret>access_key = "test"
        }
      }
    """.trimIndent())

    val variable = myFixture.getReferenceAtCaretPosition()?.resolve() as? HCLBlock
                   ?: throw AssertionError("Expected variable block")
    assertEquals("access_key", variable.name)

    val variableDirectory = variable.containingFile.virtualFile?.parent
    assertNotNull(variableDirectory)
    assertTrue("Expected directory", variableDirectory?.isDirectory == true)
    assertEquals("credentials", variableDirectory?.name)

    assertTrue(TfPsiPatterns.VariableRootBlock.accepts(variable))
  }
}
