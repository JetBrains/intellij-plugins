package org.intellij.terraform.template

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.TerraformLanguage
import org.junit.Assert

class TftplCompletionTest : BasePlatformTestCase() {
  fun `test loop variable completion`() {
    myFixture.configureByText("test.tftpl", """
      %{ for variable in provided_variables ~}
        $dollar{ <caret> }
      %{ endfor ~}
    """.trimIndent())
    myFixture.testCompletionVariants("test.tftpl", "variable")
  }

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
    Assert.assertNotNull(actualCompletions)
    UsefulTestCase.assertContainsElements(actualCompletions!!, "variable_1", "variable_2")
    UsefulTestCase.assertDoesntContain(actualCompletions, "unrelated_variable")
  }

  fun `test completion available in data language segment`() {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        $dollar{ } <caret>
      %{ endfor }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, project) {
      myFixture.completeBasicAllCarets('\n')
      myFixture.checkResult("""
        %{ for variable in another_loop_variables ~}
          $dollar{ } $dollar{variable}
        %{ endfor }
      """.trimIndent())
    }
  }

  fun `test completion available in data language segment and with no respect to prefix`() {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        "prefix<caret>suffix"
      %{ endfor }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, project) {
      myFixture.completeBasicAllCarets('\n')
      myFixture.checkResult("""
        %{ for variable in another_loop_variables ~}
          "prefix$dollar{variable}suffix"
        %{ endfor }
      """.trimIndent())
    }
  }

  fun `test completion available in injected HIL inside a data language segment`() {
    myFixture.addFileToProject("main.tf", "")
    val injectedFile = myFixture.configureByText("test.tftpl", """
      %{ for variable in another_loop_variables ~}
        resource "aws_instance" "demo_vm" {
          id = "prefix$dollar{<caret>}suffix"
        }
      %{ endfor }
    """.trimIndent())
    val psiFile = InjectedLanguageManager.getInstance(myFixture.project).getTopLevelFile(injectedFile)
    withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, project) {
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

  fun `test relevant completion for loop collection`() {
    val psiFile = myFixture.configureByText("test.tftpl", """
      %{ for variable_out in another_loop_variables ~}
        %{ for variable_in in <caret> ~}
          
        %{ endfor }
      %{ endfor }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, TerraformLanguage, project) {
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
    Assert.assertNotNull(completionVariants)
    UsefulTestCase.assertContainsElements(completionVariants!!, "first_inline", "second_inline")
  }

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
    Assert.assertNotNull(completionVariants)
    UsefulTestCase.assertContainsElements(completionVariants!!, "third_external", "fourth_external")
  }

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
    Assert.assertNotNull(completionVariants)
    UsefulTestCase.assertContainsElements(completionVariants!!, "first_inline", "second_inline", "third_external", "fourth_external")
  }

  fun `test control structures completion inside template block`() {
    myFixture.configureByText("template.tftpl", """
      %{ <caret> }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("template.tftpl")
    Assert.assertNotNull(completionVariants)
    UsefulTestCase.assertContainsElements(completionVariants!!, "for", "if", "endif", "endfor")
  }
}