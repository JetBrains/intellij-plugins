// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.openapi.ui.TestDialogManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.webSymbols.canRenameWebSymbolAtCaret
import com.intellij.webSymbols.renameWebSymbol
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angularjs.AngularTestUtil

class RenameTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "refactoring/rename"
  }

  @Throws(Exception::class)
  override fun tearDown() {
    try {
      TestDialogManager.setTestDialog(TestDialog.DEFAULT)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testRenameComponentFromStringUsage() {
    doMultiFileTest("test.component.ts", "newName")
  }

  fun testComponentFieldFromTemplate() {
    doMultiFileTest("test.component.html", "newName")
  }

  fun testI18nAttribute() {
    doMultiFileTest("directive.ts", "new-name")
  }

  fun testLocalInTemplate() {
    doMultiFileTest("test.component.html", "newName")
  }

  fun testReferenceFromTS() {
    doMultiFileTest("test.component.ts", "newReference")
  }

  fun testReferenceFromHTML() {
    doMultiFileTest("test.component.html", "newReference")
  }

  fun testReferenceFromTSNoStrings() {
    doMultiFileTest("test.component.ts", "newReference", false)
  }

  fun testReferenceFromHTMLNoStrings() {
    doMultiFileTest("test.component.html", "newReference", false)
  }

  fun testPipeFromHTML() {
    doMultiFileTest("test.component.html", "bar")
  }

  fun testPipeFromHTMLNoStrings() {
    doMultiFileTest("test.component.html", "bar", false)
  }

  fun testPipeFromTS() {
    doMultiFileTest("foo.pipe.ts", "bar")
  }

  fun testPipeFromTS2() {
    doMultiFileTest("foo.pipe.ts", "bar")
  }

  fun testPipeFromTS2NoStrings() {
    doMultiFileTest("foo.pipe.ts", "bar", false)
  }

  fun testComponentWithRelatedFiles() {
    TestDialogManager.setTestDialog(TestDialog.OK)
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { t: CodeStyleSettings ->
      t.getCustomSettings(TypeScriptCodeStyleSettings::class.java).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
      doMultiFileTest("foo-bar.component.ts", "NewNameComponent")
    }
  }

  fun testComponentFile() {
    TestDialogManager.setTestDialog(TestDialog.OK)
    doFileRename("foo-bar.component.ts", "new-name.component.ts", true)
  }

  fun testComponentToNonComponentName() {
    TestDialogManager.setTestDialog(TestDialog.OK)
    doMultiFileTest("foo-bar.component.ts", "NewNameSomething")
  }

  fun testModuleToNameWithoutPrefix() {
    TestDialogManager.setTestDialog(TestDialog.OK)
    doMultiFileTest("foo.module.ts", "Module")
  }

  fun testInjectionReparse() {
    TestDialogManager.setTestDialog(TestDialog.OK)
    doMultiFileTest("foo.component.html", "product")
  }

  fun testNgContentSelector() {
    doMultiFileTest("slots.component.ts", "new-tag")
  }

  private fun doMultiFileTest(mainFile: String, newName: String, searchCommentsAndText: Boolean = true) {
    doTest { rootDir: VirtualFile?, rootAfter: VirtualFile? ->
      myFixture.configureFromTempProjectFile(mainFile)
      if (myFixture.canRenameWebSymbolAtCaret()) {
        myFixture.renameWebSymbol(newName)
      }
      else {
        var targetElement = TargetElementUtil.findTargetElement(
          myFixture.getEditor(),
          TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
        targetElement = RenamePsiElementProcessor.forElement(targetElement!!).substituteElementToRename(targetElement,
                                                                                                        myFixture.getEditor())
        val renameProcessor = RenameProcessor(myFixture.getProject(), targetElement!!, newName, searchCommentsAndText,
                                              searchCommentsAndText)
        renameProcessor.run()
      }
    }
  }

  private fun doFileRename(mainFile: String, newName: String, searchCommentsAndText: Boolean) {
    doTest { rootDir: VirtualFile?, rootAfter: VirtualFile? ->
      val file = myFixture.configureFromTempProjectFile(mainFile)
      val renameProcessor = RenameProcessor(myFixture.getProject(), file, newName, searchCommentsAndText, searchCommentsAndText)
      renameProcessor.run()
    }
  }

  override fun getTestRoot(): String {
    return "/"
  }
}
