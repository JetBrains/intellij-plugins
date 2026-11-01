package com.intellij.terraform.template

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.platform.testFramework.assertion.collectionAssertion.CollectionAssertions.assertContainsUnordered
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.TerraformLanguage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class TftplCompletionTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun `test loop variable completion`() {
    myFixture.configureByText("test.tftpl", """
      %{ for variable in provided_variables ~}
        $dollar{ <caret> }
      %{ endfor ~}
    """.trimIndent())
    myFixture.testCompletionVariants("test.tftpl", "variable")
  }

  @Test
  fun `test nested loop variable completion`() {
    myFixture.configureByText("test.tftpl", """
      %{ for variable_1 in provided_variables_1 ~}
        %{ for variable_2 in provided_variables_2 ~}
          $dollar{ <caret> }
        %{ endfor ~}
      %{ endfor ~}
    """.trimIndent())
    myFixture.testCompletionVariants("test.tftpl", "variable_1", "variable_2")
  }

  @Test
  fun `test no unrelated completion available`() {
    myFixture.configureByText("test.tftpl", """
      %{ for unrelated_variable in another_loop_variables ~}
        $dollar{  }
      %{ endfor }

      %{ for variable_1 in provided_variables_1 ~}
        %{ for variable_2 in provided_variables_2 ~}
          $dollar{ <caret> }
        %{ endfor ~}
      %{ endfor ~}
    """.trimIndent())
    val actualCompletions = myFixture.getCompletionVariants("test.tftpl")
    assertNotNull(actualCompletions)
    assertContainsUnordered(listOf("variable_1", "variable_2"), actualCompletions!!)
    assertFalse("unrelated_variable" in actualCompletions, "unexpected 'unrelated_variable' in $actualCompletions")
  }

  @Test
  fun `test completion available in data language segment`(): Unit = timeoutRunBlocking {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        $dollar{ } <caret>
      %{ endfor }
    """.trimIndent())
    withContext(Dispatchers.EDT) {
      withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, myFixture.project) {
        myFixture.completeBasicAllCarets('\n')
        myFixture.checkResult("""
          %{ for variable in another_loop_variables ~}
            $dollar{ } $dollar{variable}
          %{ endfor }
        """.trimIndent())
      }
    }
  }

  @Test
  fun `test completion available in data language segment and with no respect to prefix`(): Unit = timeoutRunBlocking {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        "prefix<caret>suffix"
      %{ endfor }
    """.trimIndent())
    withContext(Dispatchers.EDT) {
      withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, myFixture.project) {
        myFixture.completeBasicAllCarets('\n')
        myFixture.checkResult("""
          %{ for variable in another_loop_variables ~}
            "prefix$dollar{variable}suffix"
          %{ endfor }
        """.trimIndent())
      }
    }
  }

  @Test
  fun `test completion available in injected HIL inside a data language segment`(): Unit = timeoutRunBlocking {
    myFixture.addFileToProject("main.tf", "")
    val injectedFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        resource "aws_instance" "demo_vm" {
          id = "prefix$dollar{<caret>}suffix"
        }
      %{ endfor }
    """.trimIndent())
    val psiFile = readAction { InjectedLanguageManager.getInstance(myFixture.project).getTopLevelFile(injectedFile) }
    withContext(Dispatchers.EDT) {
      withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, myFixture.project) {
        myFixture.completeBasicAllCarets('\n')
        myFixture.checkResult("""
          %{ for variable in another_loop_variables ~}
            resource "aws_instance" "demo_vm" {
              id = "prefix$dollar{variable}suffix"
            }
          %{ endfor }
        """.trimIndent())
      }
    }
  }

  @Test
  fun `test relevant completion for loop collection`(): Unit = timeoutRunBlocking {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable_out in another_loop_variables ~}
        %{ for variable_in in <caret> ~}
          
        %{ endfor }
      %{ endfor }
    """.trimIndent())
    withContext(Dispatchers.EDT) {
      withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, myFixture.project) {
        myFixture.completeBasicAllCarets('\n')
        // must be no variable_in here
        myFixture.checkResult("""
          %{ for variable_out in another_loop_variables ~}
            %{ for variable_in in variable_out ~}
              
            %{ endfor }
          %{ endfor }
        """.trimIndent())
      }
    }
  }

  @Test
  fun `test external variables completion directly from function parameter`() {
    myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content  = templatefile(
          "template.tftpl", 
          {
            first_inline = var.blabla,
            second_inline = [111, 222]
          }
        )
      }
    """.trimIndent())
    myFixture.configureByText("template.tftpl", """
      <caret>
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("template.tftpl")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("first_inline", "second_inline"), completionVariants!!)
  }

  @Test
  fun `test external variables completion from reference in function parameter`() {
    myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content = templatefile("template.tftpl", var.fromReference)
      }

      variable "fromReference" {
        default = {
          third_external = 123
          fourth_external = 456
        }
      }
    """.trimIndent())
    myFixture.configureByText("template.tftpl", """
      <caret>
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("template.tftpl")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("third_external", "fourth_external"), completionVariants!!)
  }

  @Test
  fun `test external variables completion from several template function calls`() {
    myFixture.addFileToProject("main.tf", """
      resource "resource" "example" {
        content1  = templatefile(
          "template.tftpl", 
          {
            first_inline = var.blabla,
            second_inline = [111, 222]
          }
        )
        content2 = templatefile("template.tftpl", var.fromReference)
      }

      variable "fromReference" {
        default = {
          third_external = 123
          fourth_external = 456
        }
      }
    """.trimIndent())
    myFixture.configureByText("template.tftpl", """
      <caret>
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("template.tftpl")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("first_inline", "second_inline", "third_external", "fourth_external"), completionVariants!!)
  }

  @Test
  fun `test control structures completion inside template block`() {
    myFixture.configureByText("template.tftpl", """
      %{ <caret> }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("template.tftpl")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("for", "if", "endif", "endfor"), completionVariants!!)
  }
}