// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.library.NuxtFolderModelSynchronizer

class NuxtHighlightingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/highlighting"

  override fun setUp() {
    super.setUp()
    // clear Workspace Model from `NuxtFolderEntity` created by previous tests
    NuxtFolderModelSynchronizer(project).sync()
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

    myFixture.checkHighlighting(true, false, true)
  }

  fun testNuxtGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("app.vue")

    myFixture.checkHighlighting(true, false, true)
  }

  fun testDefineNuxtComponentWorksLikeDefineComponent() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureByFile(getTestName(false) + ".vue")
    myFixture.checkHighlighting()
  }

}