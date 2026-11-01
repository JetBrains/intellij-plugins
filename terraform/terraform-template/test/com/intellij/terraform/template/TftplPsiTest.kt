package com.intellij.terraform.template

import com.intellij.lang.Language
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.terraform.template.psi.TftplFile
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.terraform.hcl.HCLLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal const val dollar = "$"

@TestApplication
class TftplTemplatePsiTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun `test no highlighting errors in loop and HCL data language`() {
    val psiFile = myFixture.configureByText("example.tftpl", """
      %{ for variable in provided_variables ~}
      resource "aws_instance" "demo_$dollar{variable}" {}
      %{ endfor ~}
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, HCLLanguage, myFixture.project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
  fun `test no highlighting errors in if expression and HCL data language`() {
    val psiFile = myFixture.configureByText("example.tftpl", """
      %{ if variable == 42 || 5 > 3 ~}
      resource "if_branch" "demo" {}
      %{~ else ~}
      resource "else_branch" "demo" {}
      %{~ endif }
    """.trimIndent())
    withDataLanguageForFile(psiFile.virtualFile, HCLLanguage, myFixture.project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
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
    withDataLanguageForFile(psiFile.virtualFile, Language.findLanguageByID("JSON")!!, myFixture.project) {
      myFixture.checkHighlighting(true, false, true)
    }
  }

  @Test
  fun `test detect data language by composite extension`() {
    val file = myFixture.configureByText("example.json.tftpl", "")
    assertEquals("JSON",
                 runReadActionBlocking { file.viewProvider.allFiles.filterNot { it is TftplFile }.singleOrNull()?.language?.id })
  }
}