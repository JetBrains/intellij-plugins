// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.javascript.testFramework.web.checkJsFileUsages
import com.intellij.lang.javascript.JSTestUtils.runInBackgroundReadActionAndWait
import com.intellij.lang.javascript.config.graph.JSGraphBuildExecutor
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@TestApplication
class NuxtFindUsagesTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/findUsages")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @AfterEach
  fun tearDown() {
    JSGraphBuildExecutor.getService(project).finishAllTasks()
  }

  @Test
  fun testScriptSetupComponentFileAllScope() {
    doTest("components/AppAlert.vue", GlobalSearchScope.everythingScope(project))
  }

  @Test
  fun testScriptSetupComponentFileProjectScope() {
    doTest("components/AppAlert.vue")
  }

  @Test
  fun testDefaultExportComponentFile() {
    doTest("components/AppAlert.vue")
  }

  private fun doTest(component: String, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)) {
    val testName = hybrid.getTestName(true)
    myFixture.copyDirectoryToProject(testName, ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NUXT_2_15_6)

    myFixture.configureFromTempProjectFile(component)
    runInBackgroundReadActionAndWait {
      myFixture.checkJsFileUsages(testName, scope = scope)
    }
  }
}
