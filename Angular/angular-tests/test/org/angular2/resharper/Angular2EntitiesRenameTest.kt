// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.resharper

import com.intellij.lang.resharper.tests.ReSharperTestUtil
import com.intellij.lang.resharper.tests.junit5.ReSharperParameterizedTestCase
import com.intellij.lang.resharper.tests.junit5.ReSharperProjectPerTest
import com.intellij.lang.resharper.tests.junit5.ReSharperTestNames
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.testFramework.renameSymbolAtCaret
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.RegistryKey
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.angular2.Angular2TestUtil
import java.io.File

@TestApplication
@RegistryKey(key = "ast.loading.filter", value = "true")
@ReSharperProjectPerTest
class Angular2EntitiesRenameTest : ReSharperParameterizedTestCase() {

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
    timeoutRunBlocking {
      withContext(Dispatchers.EDT) {
        PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments()

        //perform rename
        myFixture.renameSymbolAtCaret("zzz")

        WriteCommandAction
          .runWriteCommandAction(project) { PostprocessReformattingAspect.getInstance(project).doPostponedFormatting() }
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
    for (f in testDir.listFiles()!!) {
      if (f.getName().startsWith(testNamePrefix) && f.getName().endsWith(".gold")) {
        myFixture.checkResultByFile(f.getName().removeSuffix(".gold"),
                                    f.getName(), true)
      }
    }
  }

  companion object {
    @ReSharperTestNames
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
