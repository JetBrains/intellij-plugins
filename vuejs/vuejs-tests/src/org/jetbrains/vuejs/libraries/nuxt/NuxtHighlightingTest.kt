// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.library.getDotNuxtFolderManagerCoroutineScope
import org.jetbrains.vuejs.libraries.nuxt.library.resetDotNuxtFolderManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class NuxtHighlightingTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/highlighting")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @BeforeEach
  fun setUp() {
    resetDotNuxtFolderManager(project)
  }

  @Test
  fun testRefToStatic() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("ref-to-static", ".")
    myFixture.configureFromTempProjectFile("a/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
    myFixture.configureFromTempProjectFile("b/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testSrcDir() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("srcDir", ".")
    myFixture.configureFromTempProjectFile("client/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
  }

  /**
   * Keep in mind that Nuxt's concept of auto imports is different from WebStorm's one. It should be called implicit imports.
   */
  @Test
  fun testNuxtAutoImports() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtAutoImports", ".")
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testNuxtGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testNuxtExtendedGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtExtendedGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  @Test
  fun testDefineNuxtComponentWorksLikeDefineComponent() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile(hybrid.getTestName(false) + ".vue")
    myFixture.checkHighlighting()
  }
  
  @Test
  fun testNoErrorForUnresolvedPathAttribute() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("pathAttributeHighlighting", ".")
    myFixture.configureFromTempProjectFile("app.vue")
    myFixture.checkHighlighting(true, false, true)
  }

}

internal fun awaitDotNuxtFolderProcessing(project: Project) {
  waitCoroutinesBlocking(getDotNuxtFolderManagerCoroutineScope(project))
  IndexingTestUtil.waitUntilIndexesAreReady(project)
}
