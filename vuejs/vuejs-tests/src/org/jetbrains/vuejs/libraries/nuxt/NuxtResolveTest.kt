// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.JSTestUtils.checkResolveToDestination
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.multiResolveReference
import com.intellij.polySymbols.testFramework.resolvePolySymbolReference
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.model.impl.NuxtGlobalComponent
import org.jetbrains.vuejs.web.unwrapVueSymbolWithProximity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class NuxtResolveTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/resolve")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testComponentOverwrite(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUE_2_6_10)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.resolvePolySymbolReference("<H<caret>eaders>")
      .unwrapVueSymbolWithProximity()
      .let {
        assertInstanceOf(NuxtGlobalComponent::class.java, it)
        assertEquals("level0", (it as NuxtGlobalComponent).elementToImport?.containingFile?.virtualFile?.parent?.name)
      }
    myFixture.resolvePolySymbolReference("<F<caret>ooters>")
      .unwrapVueSymbolWithProximity()
      .let {
        assertInstanceOf(NuxtGlobalComponent::class.java, it)
        assertEquals("deep", (it as NuxtGlobalComponent).elementToImport?.containingFile?.virtualFile?.parent?.name)
      }
  }

  @Test
  fun testNoConfigFile(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUEX_4_0_0, VueTestModule.VUE_2_6_10)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureFromTempProjectFile("pages/index.vue")

    myFixture.multiResolveReference("module1.prop<caret>InModule1")
      .last().let { assertEquals("module1.js", it.containingFile.virtualFile.name) }

    myFixture.resolveReference("module2/up<caret>date")
      .let { assertEquals("module2.js", it.containingFile.virtualFile.name) }
  }

  @Test
  fun testNuxtLinkHrefIndexResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("index.vue")
  }

  @Test
  fun testNuxtLinkToDirectoryResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("insideDeclaration")
  }

  @Test
  fun testNuxtLinkHrefFileResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("component.vue")
  }

  @Test
  fun testCustomLinkHrefFileWithExtensionResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("component.vue")
  }

  @Test
  fun testNuxtLinkToNotPagesResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("index.vue")
  }

  @Test
  fun testNuxtLinkHrefUnresolvedDirectoryResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest()
  }

  @Test
  fun testNuxtJSStringTemplateValue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("component.vue")
  }

  @Test
  fun testRoutePathImportNotResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest()
  }

  @Test
  fun testPathImportResolve(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doPathResolveTest("component.vue")
  }

  @Test
  fun testPureVueProject(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    checkResolveToDestination(null, hybrid.getTestName(true), myFixture, hybrid.getTestName(false), "vue")
  }

  private fun doPathResolveTest(destination: String? = null) {
    checkResolveToDestination(destination, myFixture, hybrid.getTestName(false), "vue")
  }
}