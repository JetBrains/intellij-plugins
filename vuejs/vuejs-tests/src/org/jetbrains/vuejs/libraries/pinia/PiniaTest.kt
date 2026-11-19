// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.pinia

import com.intellij.idea.IJIgnore
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class PiniaTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/pinia")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testDefineStoreInJSFile(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("DefineStoreInJSFile", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)

    myFixture.configureFromTempProjectFile(hybrid.getTestName(false) + ".vue")
    myFixture.checkHighlighting()
    myFixture.configureFromTempProjectFile(hybrid.getTestName(false) + "_2.js")
    myFixture.checkHighlighting()
  }

  @Test
  fun testDefineStoreInJSFile_InnerVueDemi(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.copyDirectoryToProject(hybrid.getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)

    myFixture.configureFromTempProjectFile("usage_ts.ts")
    val resolveTs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    assertInstanceOf(JSProperty::class.java, resolveTs)
    assertEquals("user_ts.ts", resolveTs.containingFile.name)

    myFixture.configureFromTempProjectFile("usage_js.js")
    val resolveJs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    assertInstanceOf(JSProperty::class.java, resolveJs)
    assertEquals("user.js", resolveJs.containingFile.name)
  }

  @Test
  fun testUseStore(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22, VueTestModule.VUEUSE_9_3_0)

    myFixture.configureFromTempProjectFile("Settings.vue")
    myFixture.checkHighlighting()

    val element = JSTestUtils.getGotoDeclarationTarget(myFixture)
    assertTrue(
      PsiTreeUtil
        .findChildrenOfType(element?.containingFile, VueJSEmbeddedExpressionContent::class.java)
        .mapNotNull { TypeScriptTypeRelations.expandAndOptimizeElementTypeRecursive(it.firstChild) }
        .also { assertEquals(3, it.size) }
        .all { it.typeText == "boolean" }
    )
  }

  @IJIgnore(issue = "WEB-64531")
  @Test
  fun testUseWithMappers(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)

    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.checkHighlighting()
  }

  @Test
  fun testAddTypeFromDestructuringVar(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    // WEB-61561
    myFixture.enableInspections(JSDaemonAnalyzerFixture.defaultInspections().map { it::class.java })
    myFixture.copyDirectoryToProject(hybrid.getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)
    myFixture.configureFromTempProjectFile(hybrid.getTestName(false) + ".js")
    myFixture.checkHighlighting()
  }
}