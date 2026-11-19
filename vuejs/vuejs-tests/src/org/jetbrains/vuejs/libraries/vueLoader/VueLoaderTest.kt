// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vueLoader

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolve
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolveAlias
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.assertUnresolvedReference
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.webpack.createAndSetWebpackConfig
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueLoaderTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/vueLoader")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project
  private val testRootDisposable get() = myFixture.testRootDisposable

  @BeforeEach
  fun setUp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    val resolve = WebBundlerResolve(
      WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "src", "foo" to "src")),
      mutableListOf(myFixture.tempDirFixture.getFile(".")!!.path)
    )
    createAndSetWebpackConfig(project, resolve, testRootDisposable)
  }

  @Test
  fun testHighlighting(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java, CssUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("highlighting", ".")
    myFixture.configureFromTempProjectFile("main.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.copyDirectoryToProject("resolve", ".")
    myFixture.configureFromTempProjectFile("main.vue")
    for (test in listOf(
      Pair("src=\"./src/lo<caret>go.png", "src/logo.png"),
      Pair("src=\"./sr<caret>c/foo.png", "src"),
      Pair("src=\"./src/foo<caret>.png", null),
      Pair("src=\"@/lo<caret>go.png", "src/logo.png"),
      Pair("src=\"~@/lo<caret>go.png", "src/logo.png"),
      Pair("src=\"~foo/l<caret>ogo.png", "src/logo.png"),
      Pair("src=\"~fo<caret>o/foo.png", "src"),
      Pair("src=\"~foo/f<caret>oo.png", null),
      Pair("src=\"~ba<caret>r/logo.png", null),
      Pair("src=\"~bar_module/lo<caret>go.png", "node_modules/bar_module/logo.png"),
      Pair("src=\"~bar_module/fo<caret>o.png", null),
      Pair("url('ba<caret>r.png'", null),
      Pair("url('~@/log<caret>o.png'", "src/logo.png"),
      Pair("url('~foo/log<caret>o.png'", "src/logo.png"),
      Pair("url('~foo/ba<caret>r.png'", null)
    )) {
      try {
        if (test.second == null) {
          myFixture.assertUnresolvedReference(test.first)
        }
        else {
          assertEquals(test.second, (myFixture.resolveReference(test.first) as PsiFileSystemItem).virtualFile
            .path.removePrefix("/src/"))
        }
      } catch (e: Throwable) {
        throw AssertionError("For $test", e)
      }
    }
  }
}