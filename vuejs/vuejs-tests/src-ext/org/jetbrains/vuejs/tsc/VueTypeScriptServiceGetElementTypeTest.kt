// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.tsc

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptTypeRequestKind
import com.intellij.lang.typescript.tsc.TypeScriptServiceGetElementTypeTest
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VuePluginTypeScriptService
import org.jetbrains.vuejs.types.VueUnwrapRefType
import org.junit.Assume
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

  @Test
  override fun testCancellation() {
    // TODO Once we are able to figure out how to perform cancellation on the Volar LSP,
    //      we can enable this test. Most likely we will need to use the same approach as
    //      is in the TypeScript language server - a marker file.
    Assume.assumeTrue("Volar does not yet support cancellation", false)
    super.testCancellation()
  }

  /**
   * @see [testInstantiateMappedType]
   */
  @Test
  fun testInstantiateMappedTypeVue() {
    // language=typescript 
    val code = """
      type UnwrapRef<T> = T extends object ? { [P in keyof T]: UnwrapRef<T[P]>; } : T;
      declare function unwrap<T>(value: T): UnwrapRef<T>;
      const unwrapped = unwrap({foo: 123, bar: 456})
      unwrapp<caret>ed
    """.trimIndent()
    val vueCode = "<script setup lang='ts'>\n" + code + "\n</script>"
    myFixture.configureByText("a.vue", vueCode)
    doTestInstantiateMappedType()
  }

  // WEB-68084
  @Test
  fun testUnwrapRefType() {
    val vueCode = """
      <script setup lang='ts'>
      const a:number = 42
      </script>
    """.trimIndent()
    myFixture.configureByText("a.vue", vueCode)
    val element = JSTestUtils.findElementByText(myFixture, "a:number = 42", JSVariable::class.java)
    val jsType = calculateType(element)
    assertNotNull(jsType)
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(element) {
      val unwrapRefType = VueUnwrapRefType(jsType!!, element).substitute()
      assertEquals("number", unwrapRefType.getTypeText(JSType.TypeTextFormat.PRESENTABLE))
    }
  }

  override suspend fun sendTestCancellationCommand(service: TypeScriptService) {
    Disposer.dispose(service as VuePluginTypeScriptService)
  }
}