package org.intellij.terraform.template

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hil.psi.ForVariable
import org.intellij.terraform.template.editor.TftplVariableGoToDeclarationHandler
import org.junit.Assert

class TftplGoToDeclarationTest : BasePlatformTestCase() {
  fun `test gtd for local variable`() {
    val templateFile = myFixture.configureByText("test.tftpl", """
      %{~ for var_1 in vars ~}
        %{~ for var_2 in var_1 ~}
           %{ if var_2 }
              $dollar{var<caret>_2}
           %{ endif }
        %{~ endfor ~}
      %{~ endfor ~}
    """.trimIndent())
    val expectedTarget = templateFile.findElementWithText<ForVariable>("var_2")
    val actualTarget = GotoDeclarationAction.findTargetElement(myFixture.project, myFixture.editor, myFixture.editor.caretModel.offset)
    Assert.assertEquals(expectedTarget, actualTarget)
  }

  fun `test gtd for variables passed to template function directly`() {
    val terraformFile = myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content  = templatefile("test.tftpl", {variable = 123})
      }
    """.trimIndent())
    myFixture.configureByText("test.tftpl", """
      %{ if vari<caret>able }
      %{ endif }
    """.trimIndent())
    val expectedTarget = terraformFile.findElementWithText<HCLProperty>("variable")
    val actualTarget = GotoDeclarationAction.findTargetElement(myFixture.project, myFixture.editor, myFixture.editor.caretModel.offset)
    Assert.assertEquals(expectedTarget, actualTarget)
  }

  fun `test gtd for variables inside data language segment`() {
    val terraformFile = myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content  = templatefile("test.tftpl", {variable = 123})
      }
    """.trimIndent())
    myFixture.configureByText("test.tftpl", """
      $dollar{ vari<caret>able }
    """.trimIndent())
    val expectedTarget = terraformFile.findElementWithText<HCLProperty>("variable")
    val actualTarget = GotoDeclarationAction.findTargetElement(myFixture.project, myFixture.editor, myFixture.editor.caretModel.offset)
    Assert.assertEquals(expectedTarget, actualTarget)
  }

  fun `ignored test gtd for variables inside injected HIL language segment`() {
    val terraformFile = myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content  = templatefile("test.tftpl", {variable = 123})
      }
    """.trimIndent())
    val templateFile = myFixture.configureByText("test.tftpl", """
      id = "prefix$dollar{ vari<caret>able }suffix"
    """.trimIndent())

    val injectedVariable = templateFile.findElementWithText<PsiElement>("variable", true)
    val expectedTarget = terraformFile.findElementWithText<HCLProperty>("variable")

     val targets = TftplVariableGoToDeclarationHandler().getGotoDeclarationTargets(injectedVariable, -1, null)
    Assert.assertTrue(targets.size > 1)
    val actualTarget = targets[0]
    //val actualTarget = GotoDeclarationAction.findTargetElement(myFixture.project, myFixture.editor, myFixture.editor.caretModel.offset)
    Assert.assertEquals(expectedTarget, actualTarget)
  }

  fun `test gtd for variables passed to template function via reference`() {
    val terraformFile = myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content = templatefile("test.tftpl", var.fromReference)
      }

      variable "fromReference" {
        default = {
          variable = 123
        }
      }
    """.trimIndent())
    myFixture.configureByText("test.tftpl", """
      %{ if vari<caret>able }
      %{ endif }
    """.trimIndent())
    val expectedTarget = terraformFile.findElementWithText<HCLProperty>("variable = 123", false, 3)
    val actualTarget = GotoDeclarationAction.findTargetElement(myFixture.project, myFixture.editor, myFixture.editor.caretModel.offset)
    Assert.assertEquals(expectedTarget, actualTarget)
  }
}

private inline fun <reified T : PsiElement> PsiFile.findElementWithText(searchedText: String, injected: Boolean = false, shift: Int = searchedText.length / 2): T {
  val (foundElement, effectiveOffset) =
    if (injected) {
      val injectedPosition = text.indexOf(searchedText).takeIf { it != -1 } ?: throw AssertionError("text '$searchedText' was not found it ${name}")
      val effectiveOffset = injectedPosition + shift
      val foundElement = findElementAt(effectiveOffset)
      foundElement to effectiveOffset
    }
    else {
      val topLevelFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(this)
      val pos = topLevelFile.text.indexOf(searchedText).takeIf { it != -1 }
                ?: throw AssertionError("text '$searchedText' was not found it ${topLevelFile.name}")
      val effectiveOffset = pos + shift
      val foundElement = InjectedLanguageManager.getInstance(project)
        .getTopLevelFile(topLevelFile)
        .findElementAt(effectiveOffset)
      foundElement to effectiveOffset
    }

  Assert.assertNotNull("No element was found at offset $effectiveOffset", foundElement)
  val parentOfExpectedType = foundElement!!.parentOfType<T>()
  Assert.assertNotNull("No parent of type ${T::class.simpleName} was found at offset $effectiveOffset", parentOfExpectedType)
  return parentOfExpectedType!!
}