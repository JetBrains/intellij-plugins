package org.intellij.terraform

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.inspection.TfDuplicatedVariableInspection
import org.intellij.terraform.config.inspection.TfVARSIncorrectElementInspection
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.isFallbackVariableSearchEnabled
import org.intellij.terraform.config.model.local.TERRAFORM_LOCK_FILE_NAME
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection

class TfVarsTest : AbstractTfVarsTest(false)

class TfVarsFallbackTest : AbstractTfVarsTest(true) {
  fun testDifferentDirsWithoutLock() {

    myFixture.enableInspections(TfVARSIncorrectElementInspection::class.java)

    myFixture.configureByText("simple.tf", """
      variable "foo" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())

    val fileName = "dir/prod/prod.tfvars"
    configureByTextInDir(fileName, """
      <warning descr="Undefined variable 'foo'">foo</warning> = "9000"
      <warning descr="Undefined variable 'baz'">baz</warning> = 1
      <warning descr="Undefined variable 'bar'">bar</warning> = 0
    """.trimIndent())
    myFixture.testHighlighting(fileName)
  }

}

abstract class AbstractTfVarsTest(private val enableFallbackVariableSearchEnabled: Boolean) : BasePlatformTestCase() {

  override fun setUp() {
    super.setUp()
    val prev = AdvancedSettings.getBoolean("org.intellij.terraform.variables.search.fallback")
    AdvancedSettings.setBoolean("org.intellij.terraform.variables.search.fallback", enableFallbackVariableSearchEnabled)
    Disposer.register(testRootDisposable) {
      AdvancedSettings.setBoolean("org.intellij.terraform.variables.search.fallback", prev)
    }
  }

  fun testSameDir() {
    myFixture.enableInspections(TfVARSIncorrectElementInspection::class.java)
    myFixture.configureByText("simple.tf", """
      variable "foo" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())
    myFixture.configureByText("local.tfvars", """
      foo = "9000"
      baz = <warning descr="Incorrect variable value type. Expected: 'map'">1</warning>
      <warning descr="Undefined variable 'bar'">bar</warning> = 0
    """.trimIndent())
    myFixture.testHighlighting("local.tfvars")
  }

  fun testHCLModule() {
    myFixture.enableInspections(TfVARSIncorrectElementInspection::class.java)
    val file = myFixture.configureByText("simple.HCL", """
      variable "foo" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())
    assertEquals(Module.getModule(file).moduleRoot, file.containingDirectory)
  }


  fun testFindUsagesAndRename() {
    myFixture.configureByText("local.tfvars", """
      foo = "9000"
      baz = 1
    """.trimIndent())
    myFixture.configureByText("simple.tf", """
      variable "fo<caret>o" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())
    val usages = myFixture.findUsages(myFixture.elementAtCaret)
    assertEquals(1, usages.size)
    myFixture.renameElementAtCaret("newFoo")
    myFixture.checkResult("""
      variable "newFoo" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())
    myFixture.checkResult("local.tfvars", """
        newFoo = "9000"
        baz = 1
    """.trimIndent(), true)
  }

  fun testDifferentDirsWithLock() {

    myFixture.enableInspections(TfVARSIncorrectElementInspection::class.java)
    myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, "")

    myFixture.configureByText("simple.tf", """
      variable "foo" {
        default = "42"
        type = "string"
      }
      variable "baz" {
        type = "map"
      }
    """.trimIndent())

    val fileName = "dir/prod/prod.tfvars"
    configureByTextInDir(fileName, """
      foo = "9000"
      baz = <warning descr="Incorrect variable value type. Expected: 'map'">1</warning>
      <warning descr="Undefined variable 'bar'">bar</warning> = 0
    """.trimIndent())
    myFixture.testHighlighting(fileName)
  }

  fun testNoDifferentDirsDuplicates() {

    myFixture.enableInspections(TfDuplicatedVariableInspection::class.java)
    configureByTextInDir("dir1/$TERRAFORM_LOCK_FILE_NAME", "")

    configureByTextInDir("dir1/samedir1.tf", """
      variable "foo" {
        default = "42"
        type = "string"
      }
    """.trimIndent())

    configureByTextInDir("dir1/samedir2.tf", """
      <error descr="Variable 'foo' declared multiple times">variable "foo" {
        default = "42"
        type = "string"
      }</error>
    """.trimIndent())
    myFixture.testHighlighting("dir1/samedir2.tf")


    configureByTextInDir("dir2/file.tf", """
      variable "foo" {
        default = "42"
        type = "string"
      }
    """.trimIndent())
    myFixture.testHighlighting("dir2/file.tf")

  }

  fun testCompletionFromMultipleSources() {

    myFixture.enableInspections(TfVARSIncorrectElementInspection::class.java)
    myFixture.configureByText(TERRAFORM_LOCK_FILE_NAME, "")

    myFixture.configureByText("simple.tf", """
      variable "foo1" {
        default = "42"
        type = "string"
      }
      variable "baz1" {
        type = "map"
      }
    """.trimIndent())

    configureByTextInDir("dir/prod/another.tf", """
      variable "foo2" {
        default = "42"
        type = "string"
      }
      variable "baz2" {
        type = "map"
      }
    """.trimIndent())

    val fileName = "dir/prod/prod.tfvars"
    configureByTextInDir(fileName, """
      <caret>
    """.trimIndent())
    myFixture.testHighlighting(fileName)
    if (isFallbackVariableSearchEnabled)
      myFixture.testCompletionVariants("dir/prod/prod.tfvars", "foo1", "foo2", "baz1", "baz2")
    else
      myFixture.testCompletionVariants("dir/prod/prod.tfvars", "foo2", "baz2") // not "foo1","baz1"
  }

  fun testUnresolvedVarsNoIndex() {
    myFixture.enableInspections(HILUnresolvedReferenceInspection::class.java)

    ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
      model.contentEntries.single().excludePatterns = listOf("unindexed.tf")
    }

    myFixture.configureByText("unindexed.tf", """
      variable "test_var1" {
        default = ""
      }
      
      locals = {
        test = var.test_var1
        test2 = var.<error descr="Unresolved reference test_var2">test_var2</error>
      }
    """.trimIndent())

    myFixture.checkHighlighting()
  }

  protected fun configureByTextInDir(fileName: String, text: String) {
    WriteAction.compute<VirtualFile, Throwable> {
      val prodTfvarsFile: VirtualFile = myFixture.tempDirFixture.createFile(fileName)
      VfsUtil.saveText(prodTfvarsFile, text)
      prodTfvarsFile
    }
  }

}