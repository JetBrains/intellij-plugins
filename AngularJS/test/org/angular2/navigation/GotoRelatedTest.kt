// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation

import com.intellij.ide.actions.GotoRelatedSymbolAction
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.presentation.java.SymbolPresentationUtil
import com.intellij.webSymbols.moveToOffsetBySignature
import one.util.streamex.StreamEx
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@RunWith(com.intellij.testFramework.Parameterized::class)
class GotoRelatedTest : Angular2CodeInsightFixtureTestCase() {
  @Parameterized.Parameter
  @JvmField
  var myTestDir: String? = null
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "related/" + myTestDir
  }

  @Test
  fun singleTest() {
    myFixture.setCaresAboutInjection(false)
    myFixture.copyDirectoryToProject(".", ".")
    val testFile = myFixture.findFileInTempDir("test.txt")
    val result: MutableList<String> = ArrayList()
    try {
      BufferedReader(InputStreamReader(testFile.getInputStream(), StandardCharsets.UTF_8)).use { reader ->
        var line = ""
        while (reader.readLine()?.also { line = it } != null) {
          if (line.trim { it <= ' ' }.isEmpty() || line.startsWith(" ")) {
            continue
          }
          result.add(line)
          val input = StringUtil.split(line, "#")
          assert(!input.isEmpty())
          myFixture.configureFromTempProjectFile(input[0])
          if (input.size > 1) {
            myFixture.moveToOffsetBySignature(input[1].replace("{caret}", "<caret>"))
          }
          stringifiedRelatedItems.mapTo(result) { " $it" }
        }
      }
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
    myFixture.configureFromTempProjectFile("test.txt")
    WriteAction.runAndWait<RuntimeException> { myFixture.getDocument(myFixture.getFile()).setText(StringUtil.join(result, "\n")) }
    myFixture.checkResultByFile("test.txt")
  }

  private val stringifiedRelatedItems: List<String>
    get() = GotoRelatedSymbolAction.getItems(myFixture.getFile(), myFixture.getEditor(), null)
      .asSequence()
      .filter { it.group == "Angular Component" }
      .map { item ->
        val presentation = getPresentation(item.element!!)
        val file = item.element!!.getContainingFile()
        val name = item.customName ?: SymbolPresentationUtil.getSymbolPresentableText(item.element!!)
        val location = if (file != null && name != file.getName()) "(" + file.getName() + ")" else null
        (item.mnemonic.toString() + ". "
         + name + " "
         + (item.customContainerName ?: location) + " <"
         + presentation.getPresentableText() + ", "
         + presentation.locationString?.let { FileUtil.toSystemIndependentName(it) } + ">")
      }
      .toList()

  companion object {
    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    @JvmStatic
    @Suppress("unused")
    fun testNames(klass: Class<*>): List<String> {
      return StreamEx.of(*File(AngularTestUtil.getBaseTestDataPath(klass), "related").listFiles())
        .map { obj: File -> obj.getName() }
        .sorted()
        .toList()
    }

    @Parameterized.Parameters
    @JvmStatic
    fun data(): Collection<Any> {
      return ArrayList()
    }

    private fun getPresentation(element: PsiElement): ItemPresentation {
      return if (element is NavigationItem) {
        (element as NavigationItem).getPresentation()!!
      }
      else element as ItemPresentation
    }
  }
}
