// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.javascript.testFramework.web.checkFileUsages
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath

class NuxtFindUsagesTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/findUsages"

  fun testScriptSetupComponentFileAllScope() {
    doTest("components/AppAlert.vue", GlobalSearchScope.everythingScope(project))
  }

  fun testScriptSetupComponentFileProjectScope() {
    doTest("components/AppAlert.vue")
  }

  fun testDefaultExportComponentFile() {
    doTest("components/AppAlert.vue")
  }

  private fun doTest(component: String, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)) {
    val testName = getTestName(true)
    myFixture.copyDirectoryToProject(testName, ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NUXT_2_15_6)

    myFixture.configureFromTempProjectFile(component)
    myFixture.checkFileUsages(testName, scope = scope)
  }
}