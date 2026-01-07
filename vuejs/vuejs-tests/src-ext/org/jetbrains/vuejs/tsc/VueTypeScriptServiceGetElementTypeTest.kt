// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.tsc

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.tsc.TypeScriptServiceGetElementTypeTest
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VuePluginTypeScriptService
import org.jetbrains.vuejs.types.VueUnwrapRefType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class VueTypeScriptServiceGetElementTypeTest :
  TypeScriptServiceGetElementTypeTest() {

  @JvmField
  @Rule
  val rule: TestRule = TrackFailedTestRule(
    "testObjectLiteralWithSymbol",
    "testAnonymousThread"
  )

  override fun setUpTypeScriptService() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    TypeScriptServiceTestMixin.setUpTypeScriptService(myFixture) {
      it is VuePluginTypeScriptService
    }
  }

  override fun calculateType(element: PsiElement, typeRequestKind: TypeScriptTypeRequestKind): JSType? {
    return super.calculateType(element, typeRequestKind).also {
      assertInstanceOf(TypeScriptServiceHolder.getForFile(project, file.virtualFile), VuePluginTypeScriptService::class.java)
    }
  }

  /**
   * @see [testInstantiateMappedType]
   */
  @Test
  fun testInstantiateMappedTypeVue() {
    // language=vue
    val code = """
    <script setup lang="ts">
    type UnwrapRef<T> = T extends object ? { [P in keyof T]: UnwrapRef<T[P]>; } : T;
    declare function unwrap<T>(value: T): UnwrapRef<T>;
    const unwrapped = unwrap({foo: 123, bar: 456})
    unwrapp<caret>ed
    </script>
    """.trimIndent()
    myFixture.configureByText("App.vue", code)
    doTestInstantiateMappedType()
  }

  // WEB-68084
  @Test
  fun testUnwrapRefType() {
    // language=vue
    val code = """
    <script setup lang="ts">
    const a:number = 42
    </script>
    """.trimIndent()
    myFixture.configureByText("App.vue", code)
    val element = JSTestUtils.findElementByText(myFixture, "a:number = 42", JSVariable::class.java)
    val jsType = calculateType(element)
    assertNotNull(jsType)
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
      val unwrapRefType = VueUnwrapRefType(jsType!!, element).substitute()
      assertEquals("number", unwrapRefType.getTypeText(JSType.TypeTextFormat.PRESENTABLE))
    }
  }
}