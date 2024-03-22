// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.tsc

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAliasTypeImpl
import com.intellij.lang.javascript.psi.types.TypeScriptMappedJSTypeImpl
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.tsc.TypeScriptServiceGetElementTypeTest
import com.intellij.lang.typescript.tsc.TypeScriptServiceTestMixin
import com.intellij.platform.lsp.tests.waitUntilFileOpenedByLspServer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.intellij.lang.annotations.Language
import org.jetbrains.vuejs.lang.typescript.service.markVolarForceEnabled
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService

class VolarTypeScriptServiceGetElementTypeTest : TypeScriptServiceGetElementTypeTest() {

  override fun setUpTypeScriptService() {
    markVolarForceEnabled(true)
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

  // FIXME Mapped type js type declaration is not ready
  /**
   * @see [testInstantiateMappedType]
   */
  fun testInstantiateMappedTypeVue() = TypeScriptServiceTestMixin.temporarilyDisableJsTypeDeclarations {
    //myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    @Language("TypeScript") val code = """
      type UnwrapRef<T> = T extends object ? { [P in keyof T]: UnwrapRef<T[P]>; } : T;
      declare function unwrap<T>(value: T): UnwrapRef<T>;
      const unwrapped = unwrap({foo: 123, bar: 456})
      unwrapp<caret>ed
    """.trimIndent()
    val vueCode = "<script setuo lang='ts'>\n" + code + "\n</script>"
    myFixture.configureByText("a.vue", vueCode)

    val ref = PsiTreeUtil.getParentOfType(myFixture.file.findElementAt(myFixture.caretOffset), JSReferenceExpression::class.java, false)!!
    val type = calculateType(ref, true)

    UsefulTestCase.assertInstanceOf(type, JSAliasTypeImpl::class.java)
    type as JSAliasTypeImpl
    UsefulTestCase.assertInstanceOf(type.alias, TypeScriptMappedJSTypeImpl::class.java)
    UsefulTestCase.assertInstanceOf(type.originalType, JSRecordType::class.java)

    val recordType = type.originalType as JSRecordType
    TestCase.assertEquals("{foo: 123, bar: 456}", recordType.sourceElement?.text)
    TestCase.assertEquals(
      setOf("foo: 123", "bar: 456"),
      recordType.properties.map { it.memberSource.singleElement?.text }.toSet()
    )
  }

}