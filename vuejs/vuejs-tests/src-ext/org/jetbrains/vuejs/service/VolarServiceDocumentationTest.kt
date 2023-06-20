// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.Test

/**
 * @see com.intellij.lang.javascript.typescript.service.TypeScriptServiceDocumentationTest
 */
class VolarServiceDocumentationTest : VolarServiceTestBase() {
  override fun getBasePath(): String = vueRelativeTestDataPath() + "/service/documentation"

  override fun setUp() {
    super.setUp()
    RegistryManager.getInstance().get("typescript.show.own.type").setValue(true, testRootDisposable)
  }

  @Test
  fun testNullChecks() = defaultQuickNavigateTest()

  @Test
  fun testTypeNarrowing() = defaultQuickNavigateTest()

  @Test
  fun testQualifiedReference() = defaultQuickNavigateTest()

  @Test
  fun testGenericType() = defaultQuickNavigateTest()

  fun testRefFunction() = defaultQuickNavigateTest()

  fun testRefUnwrapping() = defaultQuickNavigateTest()

  // todo test injected interpolations after implementing support for them

  private fun defaultQuickNavigateTest(directory: Boolean = false) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByFile(getTestName(false) + "." + extension)
    myFixture.checkLspHighlighting()
    assertCorrectService()

    val doc = JSAbstractDocumentationTest.getQuickNavigateText(myFixture)
    JSAbstractDocumentationTest.checkExpected(doc, testDataPath + "/" + getTestName(false) + ".expected.html")
  }
}
