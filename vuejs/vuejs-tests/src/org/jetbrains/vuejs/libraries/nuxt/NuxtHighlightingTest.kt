// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.openapi.project.Project
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.library.getDotNuxtFolderManagerCoroutineScope
import org.jetbrains.vuejs.libraries.nuxt.library.resetDotNuxtFolderManager

class NuxtHighlightingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/highlighting"

  override fun setUp() {
    super.setUp()
    resetDotNuxtFolderManager(project)
  }

  fun testRefToStatic() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("ref-to-static", ".")
    myFixture.configureFromTempProjectFile("a/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
    myFixture.configureFromTempProjectFile("b/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
  }

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
  fun testNuxtAutoImports() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtAutoImports", ".")
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  fun testNuxtGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  fun testNuxtExtendedGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtExtendedGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    myFixture.checkHighlighting(true, false, true)
  }

  fun testDefineNuxtComponentWorksLikeDefineComponent() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile(getTestName(false) + ".vue")
    myFixture.checkHighlighting()
  }
  
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
