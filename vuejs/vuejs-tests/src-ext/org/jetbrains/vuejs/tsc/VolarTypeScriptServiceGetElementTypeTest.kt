// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.tsc

import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.tsc.TypeScriptServiceGetElementTypeTest
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.psi.PsiElement
import com.intellij.testFramework.UsefulTestCase
import org.intellij.lang.annotations.Language
import org.jetbrains.vuejs.lang.typescript.service.VueServiceSetActivationRule
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class VolarTypeScriptServiceGetElementTypeTest : TypeScriptServiceGetElementTypeTest() {

  @JvmField
  @Rule
  val rule: TestRule = TrackFailedTestRule(
    "testObjectLiteralWithSymbol",
    "testInWriteAction",
    "testAnonymousThread"
  )
  
  override fun setUpTypeScriptService() {
    VueServiceSetActivationRule.markForceEnabled(true)
    TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
      it is VolarTypeScriptService
    }
  }

  override fun calculateType(element: PsiElement, useTsc: Boolean): JSType? {
    waitUntilFileOpenedByLspServer(project, file.virtualFile)

    return super.calculateType(element, useTsc).also {
      UsefulTestCase.assertInstanceOf(TypeScriptService.getForFile(project, file.virtualFile), VolarTypeScriptService::class.java)
    }
  }

  /**
   * @see [testInstantiateMappedType]
   */
  @Test
  fun testInstantiateMappedTypeVue() {
    //myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    @Language("TypeScript") val code = """
      type UnwrapRef<T> = T extends object ? { [P in keyof T]: UnwrapRef<T[P]>; } : T;
      declare function unwrap<T>(value: T): UnwrapRef<T>;
      const unwrapped = unwrap({foo: 123, bar: 456})
      unwrapp<caret>ed
    """.trimIndent()
    val vueCode = "<script setup lang='ts'>\n" + code + "\n</script>"
    myFixture.configureByText("a.vue", vueCode)
    doTestInstantiateMappedType()
  }

}