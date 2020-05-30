// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.pug

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VuePugFoldingTest : BasePlatformTestCase() {
  fun testPugInTemplate() {
    myFixture.testFolding(PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/pug/" + getTestName(false) + ".pug")
  }
}