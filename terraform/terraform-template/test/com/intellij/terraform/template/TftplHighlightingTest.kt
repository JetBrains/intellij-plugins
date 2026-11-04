package com.intellij.terraform.template

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.platform.testFramework.assertion.collectionAssertion.CollectionAssertions.assertContainsUnordered
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.PsiDocumentManager
import com.intellij.terraform.template.editor.MaybeTfTemplateInspection
import com.intellij.terraform.template.editor.TfUnselectedDataLanguageInspection
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class TftplHighlightingTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    myFixture.enableInspections(TfUnselectedDataLanguageInspection::class.java,
                                MaybeTfTemplateInspection::class.java)
  }

  @Test
  fun `test detect unselected data language`() {
    val errorMessage = TftplBundle.message("inspection.unselected.data.language.name")
    myFixture.configureByText("test.tftpl", """
      %{if condition}<warning descr="$errorMessage">
        Hello world!
      </warning>%{endif}
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun `test detect template with custom extension`(): Unit = timeoutRunBlocking {
    myFixture.addFileToProject("main.tf", """
      locals {
        test = templatefile("$dollar{path.module}/noExtension.json", {
          values = [111, 222]
        })
      }
    """.trimIndent())
    val errorMessage = TftplBundle.message("inspection.possible.template.name")
    myFixture.configureByText("noExtension.json", """
      <warning descr="$errorMessage"><error descr="<literal>, IDENTIFIER, '[' or '{' expected, got '%'">%</error>{if condition}
        console.log("Hello world!")
        debugger
      %{endif}</warning>
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
    withContext(Dispatchers.EDT) {
      val considerAsTemplateIntention = myFixture.getAvailableIntention(TftplBundle.message("inspection.possible.template.add.association.fix.name"))
      assertNotNull(considerAsTemplateIntention)
      considerAsTemplateIntention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)

      PsiDocumentManager.getInstance(myFixture.project).reparseFiles(listOf(myFixture.file.virtualFile), true)
    }
    myFixture.checkResult("""
      %{if condition}
        console.log("Hello world!")
        debugger
      %{endif}
    """.trimIndent())

    assertContainsUnordered(listOf("JSON", "TFTPL"), readAction { myFixture.file.viewProvider.allFiles.map { it.language.id } })
  }
}