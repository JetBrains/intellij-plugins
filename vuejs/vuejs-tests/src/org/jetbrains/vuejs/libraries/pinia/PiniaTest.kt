// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.pinia

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.lang.getVueTestDataPath

class PiniaTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/pinia"

  fun testDefineStoreInJSFile() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("DefineStoreInJSFile", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)

    myFixture.configureFromTempProjectFile(getTestName(false) + ".vue")
    myFixture.checkHighlighting()
    myFixture.configureFromTempProjectFile(getTestName(false) + "_2.js")
    myFixture.checkHighlighting()
  }

  fun testDefineStoreInJSFile_InnerVueDemi() {
    myFixture.copyDirectoryToProject(getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22)

    myFixture.configureFromTempProjectFile("usage_ts.ts")
    val resolveTs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    UsefulTestCase.assertInstanceOf(resolveTs, JSProperty::class.java)
    TestCase.assertEquals("user_ts.ts", resolveTs.containingFile.name)

    myFixture.configureFromTempProjectFile("usage_js.js")
    val resolveJs = JSTestUtils.getGotoDeclarationTarget(myFixture)!!
    UsefulTestCase.assertInstanceOf(resolveJs, JSProperty::class.java)
    TestCase.assertEquals("user.js", resolveJs.containingFile.name)
  }

  fun testUseStore() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(getTestName(false), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PINIA_2_0_22, VueTestModule.VUEUSE_9_3_0)

    myFixture.configureFromTempProjectFile("Settings.vue")
    myFixture.checkHighlighting()

    val element = JSTestUtils.getGotoDeclarationTarget(myFixture)
    TestCase.assertTrue(
      PsiTreeUtil
        .findChildrenOfType(element?.containingFile, VueJSEmbeddedExpression::class.java)
        .mapNotNull { TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(JSResolveUtil.getElementJSType(it.firstChild)) }
        .also { TestCase.assertEquals(3, it.size) }
        .all { it.typeText == "boolean" }
    )
  }

}