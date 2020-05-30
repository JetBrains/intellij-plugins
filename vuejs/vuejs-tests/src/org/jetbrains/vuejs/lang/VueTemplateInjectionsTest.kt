// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.application.PathManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
import junit.framework.TestCase
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.*

@RunWith(com.intellij.testFramework.Parameterized::class)
class VueTemplateInjectionsTest : BasePlatformTestCase() {

  @Parameterized.Parameter
  @JvmField
  var myFileName: String? = null

  @Test
  fun doSingleTest() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFile(myFileName!!)

    val injectedLanguageManager = InjectedLanguageManager.getInstance(project)

    invokeTestRunnable {
      PsiDocumentManager.getInstance(project).commitAllDocuments()

      TestCase.assertEquals(VueLanguage.INSTANCE, injectedLanguageManager.findInjectedElementAt(
        myFixture.file, myFixture.file.findOffsetBySignature("</<caret>div>"))?.containingFile?.language)

      val injectedElement = injectedLanguageManager.findInjectedElementAt(
        myFixture.file, myFixture.file.findOffsetBySignature("<caret>title + foo"))

      TestCase.assertNotNull(PsiTreeUtil.getParentOfType(injectedElement, VueJSEmbeddedExpression::class.java))

      val resolved = (injectedElement!!.parent as JSReferenceExpression).resolve()
      TestCase.assertTrue(resolved!!.text, resolved.text.contains("Check me"))

    }
  }

  @Before
  @Throws(Exception::class)
  public override fun setUp() {
    super.setUp()
  }

  @After
  public override fun tearDown() {
    EdtTestUtil.runInEdtAndWait(ThrowableRunnable { super.tearDown() })
  }

  override fun getTestDataPath(): String {
    return Companion.testDataPath
  }

  companion object {
    val testDataPath: String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/injection/template/"

    @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun testNames(@Suppress("UNUSED_PARAMETER") klass: Class<*>): List<String> {
      val testData = File(testDataPath)
      return StreamEx.of<File>(*testData.listFiles()!!)
        .filter { file -> file.name.endsWith("js") || file.name.endsWith("ts") }
        .map { file -> file.name }
        .toList()
    }

    @Parameterized.Parameters
    @JvmStatic
    fun data(): Collection<Any> {
      return ArrayList()
    }
  }

}
