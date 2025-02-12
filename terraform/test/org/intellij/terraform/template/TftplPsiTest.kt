package org.intellij.terraform.template

import com.intellij.lang.Language
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.template.psi.TftplFile
import org.junit.Assert

internal const val dollar = "$"

class TftplTemplatePsiTest : BasePlatformTestCase() {
  fun `test no highlighting errors in loop and HCL data language`() {
    val psiFile = myFixture.configureByText("example.tftpl", """
      %{ for variable in provided_variables ~}
      resource "aws_instance" "demo_$dollar{variable}" {}
      %{ endfor ~}
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, HCLLanguage, project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  fun `test no highlighting errors in if expression and HCL data language`() {
    val psiFile = myFixture.configureByText("example.tftpl", """
      %{ if variable == 42 || 5 > 3 ~}
      resource "if_branch" "demo" {}
      %{~ else ~}
      resource "else_branch" "demo" {}
      %{~ endif }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, HCLLanguage, project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  fun `test if expressions with JSON data language`() {
    val psiFile = myFixture.configureByText("example.tftpl", """
      {
        %{ if cradle_v != "" }
        "cradle": 123,
        %{ endif }
        %{ if jade_v != "" }
        "jade": 456
        %{ endif }
      }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, Language.findLanguageByID("JSON")!!, project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  fun `test detect data language by composite extension`() {
    val file = myFixture.configureByText("example.json.tftpl", "")
    Assert.assertEquals("JSON",
                        file.viewProvider.allFiles.filterNot { it is TftplFile }.singleOrNull()?.language?.id)
  }
}