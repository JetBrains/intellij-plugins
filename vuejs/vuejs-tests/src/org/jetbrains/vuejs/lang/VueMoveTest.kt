// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSMoveTestBase
import com.intellij.openapi.application.PathManager

class VueMoveTest: JSMoveTestBase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/refactoring/move/"

  override fun getTestRoot(): String {
    return ""
  }

  fun testTsPathMappings() {
    doTest("src/components/ImportedFile.vue", "src/components/sub-directory")
  }
}