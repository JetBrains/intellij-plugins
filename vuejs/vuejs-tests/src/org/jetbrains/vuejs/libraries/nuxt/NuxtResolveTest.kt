// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.polySymbols.testFramework.multiResolveReference
import com.intellij.polySymbols.testFramework.resolvePolySymbolReference
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.polySymbols.testFramework.resolveToPolySymbolSource
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.model.impl.NuxtGlobalComponent

class NuxtResolveTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/resolve"

  fun testComponentOverwrite() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUE_2_6_10)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.resolvePolySymbolReference("<H<caret>eaders>")
      .let {
        assertInstanceOf(it, NuxtGlobalComponent::class.java)
        TestCase.assertEquals("level0", (it as NuxtGlobalComponent).elementToImport?.containingFile?.virtualFile?.parent?.name)
      }
    myFixture.resolvePolySymbolReference("<F<caret>ooters>")
      .let {
        assertInstanceOf(it, NuxtGlobalComponent::class.java)
        TestCase.assertEquals("deep", (it as NuxtGlobalComponent).elementToImport?.containingFile?.virtualFile?.parent?.name)
      }
  }

  fun testNoConfigFile() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUEX_4_0_0, VueTestModule.VUE_2_6_10)
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("pages/index.vue")

    myFixture.multiResolveReference("module1.prop<caret>InModule1")
      .last().let { TestCase.assertEquals("module1.js", it.containingFile.virtualFile.name) }

    myFixture.resolveReference("module2/up<caret>date")
      .let { TestCase.assertEquals("module2.js", it.containingFile.virtualFile.name) }
  }

  fun testNuxtLinkHrefIndexResolve() {
    doPathResolveTest("index.vue")
  }

  fun testNuxtLinkToDirectoryResolve() {
    doPathResolveTest("insideDeclaration")
  }

  fun testNuxtLinkHrefFileResolve() {
    doPathResolveTest("component.vue")
  }

  fun testCustomLinkHrefFileWithExtensionResolve() {
    doPathResolveTest("component.vue")
  }

  fun testNuxtLinkToNotPagesResolve() {
    doPathResolveTest("index.vue")
  }

  fun testNuxtLinkHrefUnresolvedDirectoryResolve() {
    doPathResolveTest()
  }

  fun testNuxtJSStringTemplateValue() {
    doPathResolveTest("component.vue")
  }

  fun testRoutePathImportNotResolve() {
    doPathResolveTest()
  }

  fun testPathImportResolve() {
    doPathResolveTest("component.vue")
  }

  fun testPureVueProject() {
    checkResolveToDestination(null, getTestName(true), myFixture, getTestName(false), "vue")
  }

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, getTestName(false), "vue")
  }
}