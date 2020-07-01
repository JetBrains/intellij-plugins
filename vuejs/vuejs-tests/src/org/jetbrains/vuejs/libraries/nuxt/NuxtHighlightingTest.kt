// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NuxtHighlightingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/nuxt/highlighting"

  fun testRefToStatic() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("ref-to-static", ".")
    myFixture.configureFromTempProjectFile("a/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
    myFixture.configureFromTempProjectFile("b/page/test.vue")
    myFixture.checkHighlighting(true, false, true)
  }
}