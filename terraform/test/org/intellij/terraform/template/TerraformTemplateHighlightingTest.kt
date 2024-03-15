package org.intellij.terraform.template

import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.template.editor.MaybeTerraformTemplateInspection
import org.intellij.terraform.template.editor.TerraformUnselectedDataLanguageInspection
import org.junit.Assert

class TerraformTemplateHighlightingTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(TerraformUnselectedDataLanguageInspection::class.java,
                                MaybeTerraformTemplateInspection::class.java)
  }

  fun `test detect unselected data language`() {
    myFixture.configureByText("test.tftpl", """
      %{if condition}<warning descr="No template data language selected">
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
    myFixture.configureByText("noExtension.json", """
      <warning descr="Possibly template file"><error descr="<value> expected, got '%'">%</error>{if condition}
        console.log("Hello world!")
        debugger
      %{endif}</warning>
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
    val considerAsTemplateIntention = myFixture.getAvailableIntention(HCLBundle.message("inspection.possible.template.add.association.fix.name"))
    Assert.assertNotNull(considerAsTemplateIntention)
    considerAsTemplateIntention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)

    myFixture.checkResult("""
      %{if condition}
        console.log("Hello world!")
        debugger
      %{endif}
    """.trimIndent())

    UsefulTestCase.assertContainsElements(myFixture.file.viewProvider.allFiles.map { it.language.id }, listOf("JSON", "TFTPL"))
  }
}