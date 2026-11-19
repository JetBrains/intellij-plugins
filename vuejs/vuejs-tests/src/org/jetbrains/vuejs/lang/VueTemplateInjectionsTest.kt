// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.application.readAction
import com.intellij.polySymbols.testFramework.findOffsetBySignature
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueTemplateInjectionsTest {
  private val hybrid by hybridFixture(testDataPath)
  private val myFixture get() = hybrid.fixture

  @ParameterizedTest(name = "{0}")
  @MethodSource("testNames")
  fun doSingleTest(myFileName: String): Unit = timeoutRunBlocking(timeout = 2.minutes) {
    myFixture.configureVueDependencies()
    myFixture.configureByFile(myFileName)

    readAction {
      val injectedLanguageManager = InjectedLanguageManager.getInstance(myFixture.project)

      assertEquals(VueLanguage, injectedLanguageManager.findInjectedElementAt(
        myFixture.file, myFixture.file.findOffsetBySignature("</<caret>div>"))?.containingFile?.language)

      val injectedElement = injectedLanguageManager.findInjectedElementAt(
        myFixture.file, myFixture.file.findOffsetBySignature("<caret>title + foo"))

      assertNotNull(PsiTreeUtil.getParentOfType(injectedElement, VueJSEmbeddedExpressionContent::class.java))

      val resolved = (injectedElement!!.parent as JSReferenceExpression).resolve()?.context
      assertTrue(resolved!!.text.contains("Check me"), resolved.text)
    }
  }

  companion object {
    val testDataPath: String = getVueTestDataPath() + "/injection/template/"

    @JvmStatic
    fun testNames(): List<String> =
      File(testDataPath).listFiles()!!
        .filter { file -> file.name.endsWith("js") || file.name.endsWith("ts") }
        .map { file -> file.name }
  }

}
