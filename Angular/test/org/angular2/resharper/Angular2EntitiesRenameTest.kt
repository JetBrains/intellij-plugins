// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.lang.resharper.ReSharperParameterizedTestCase
import com.intellij.lang.resharper.ReSharperTestUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.refactoring.rename.RenameProcessor
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.testFramework.Parameterized
import com.intellij.webSymbols.testFramework.canRenameWebSymbolAtCaret
import com.intellij.webSymbols.testFramework.renameWebSymbol
import org.angular2.Angular2TestUtil
import org.junit.runner.RunWith
import java.io.File

@RunWith(value = Parameterized::class)
@Suppress("ACCIDENTAL_OVERRIDE")
class Angular2EntitiesRenameTest : ReSharperParameterizedTestCase() {

  public override fun setUp() {
    super.setUp()
    Angular2TestUtil.enableAstLoadingFilter(this)
  }

  override fun doSingleTest(testFile: String, path: String) {
    myFixture.copyFileToProject("../../../package.json", "package.json")
    val testDir = File(testDataPath)
    val testNamePrefix = "$name."
    var fileWithCaret: String? = null
    for (f in testDir.listFiles()!!) {
      if (f.getName().startsWith(testNamePrefix) && !f.getName().endsWith(".gold")) {
        val text = ReSharperTestUtil.loadAndConvertCaret(f.getName(), testDir.path)
        if (text.contains("<caret>")) {
          fileWithCaret = f.getName()
        }
        else {
          myFixture.configureByText(f.getName(), text)
        }
      }
    }
    assert(fileWithCaret != null)
    myFixture.configureByText(fileWithCaret!!, ReSharperTestUtil.loadAndConvertCaret(fileWithCaret, testDir.path))
    PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments()

    //perform rename
    if (myFixture.canRenameWebSymbolAtCaret()) {
      myFixture.renameWebSymbol("zzz")
    }
    else {
      var targetElement = TargetElementUtil.findTargetElement(
        myFixture.getEditor(),
        TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
      targetElement = RenamePsiElementProcessor.forElement(targetElement!!).substituteElementToRename(targetElement, myFixture.getEditor())
      val renameProcessor = RenameProcessor(myFixture.getProject(), targetElement!!, "zzz", false, false)
      renameProcessor.run()
      WriteCommandAction
        .runWriteCommandAction(project) { PostprocessReformattingAspect.getInstance(project).doPostponedFormatting() }
      FileDocumentManager.getInstance().saveAllDocuments()
    }
    for (f in testDir.listFiles()!!) {
      if (f.getName().startsWith(testNamePrefix) && f.getName().endsWith(".gold")) {
        myFixture.checkResultByFile(f.getName().removeSuffix(".gold"),
                                    f.getName(), true)
      }
    }
  }

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @Throws(Exception::class)
    @JvmStatic
    @Suppress("unused")
    fun testNames(klass: Class<*>): List<String> {
      return ReSharperTestUtil.getTestParamsFromSubPath(callFindTestData(klass))
        .asSequence()
        .map { name: String? ->
          StringUtil.split(
            name!!, ".")[0]
        }
        .distinct()
        .toList()
    }

    @JvmStatic
    @Suppress("unused", "UNUSED_PARAMETER")
    fun findTestData(klass: Class<*>): String {
      return Angular2TestUtil.getBaseTestDataPath() + "resharper/Refactorings/Rename/Angular2"
    }
  }
}
