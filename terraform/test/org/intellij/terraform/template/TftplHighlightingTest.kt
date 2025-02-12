package org.intellij.terraform.template

import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.template.editor.MaybeTfTemplateInspection
import org.intellij.terraform.template.editor.TfUnselectedDataLanguageInspection
import org.junit.Assert

class TftplHighlightingTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(TfUnselectedDataLanguageInspection::class.java,
                                MaybeTfTemplateInspection::class.java)
  }

  fun `test detect unselected data language`() {
    val errorMessage = HCLBundle.message("inspection.unselected.data.language.name")
    myFixture.configureByText("test.tftpl", """
      %{if condition}<warning descr="$errorMessage">
        Hello world!
      </warning>%{endif}
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
  }

  fun `test detect template with custom extension`() {
    myFixture.addFileToProject("main.tf", """
      locals {
        test = templatefile("$dollar{path.module}/noExtension.json", {
          values = [111, 222]
        })
      }
    """.trimIndent())
    val errorMessage = HCLBundle.message("inspection.possible.template.name")
    myFixture.configureByText("noExtension.json", """
      <warning descr="$errorMessage"><error descr="<value> expected, got '%'">%</error>{if condition}
        console.log("Hello world!")
        debugger
      %{endif}</warning>
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
    val considerAsTemplateIntention = myFixture.getAvailableIntention(HCLBundle.message("inspection.possible.template.add.association.fix.name"))
    Assert.assertNotNull(considerAsTemplateIntention)
    considerAsTemplateIntention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)

    PsiDocumentManager.getInstance(myFixture.project).reparseFiles(listOf(myFixture.file.virtualFile), true)
    myFixture.checkResult("""
      %{if condition}
        console.log("Hello world!")
        debugger
      %{endif}
    """.trimIndent())

    UsefulTestCase.assertContainsElements(myFixture.file.viewProvider.allFiles.map { it.language.id }, listOf("JSON", "TFTPL"))
  }
}