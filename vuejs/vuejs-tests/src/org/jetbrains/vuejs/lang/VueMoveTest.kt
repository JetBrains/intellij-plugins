// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSMoveTestBase
import com.intellij.openapi.util.registry.Registry

open class VueMoveTest: JSMoveTestBase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/refactoring/move/"

  override fun getTestRoot(): String {
    return ""
  }

  fun testTsPathMappings() {
    doTest("src/components/ImportedFile.vue", "src/components/sub-directory")
  }

  fun testIndexFile() {
    doTest("declared/subdir/index.vue", "declared")
  }


  class BranchTest : VueMoveTest() {
    override fun setUp() {
      super.setUp()
      Registry.get("run.refactorings.in.model.branch").setValue(true, testRootDisposable)
    }
  }

}