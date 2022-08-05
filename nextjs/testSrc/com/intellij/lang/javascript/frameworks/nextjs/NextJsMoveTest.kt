package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSMoveTestBase
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.registry.Registry

open class NextJsMoveTest: JSMoveTestBase()  {

  class BranchTest : NextJsMoveTest() {
    @Throws(Exception::class)
    override fun setUp() {
      super.setUp()
      Registry.get("run.refactorings.in.model.branch").setValue(true, testRootDisposable)
    }
  }

  override fun getTestDataPath(): String {
    return PathManager.getHomePath() + "/contrib/nextjs/testData/"
  }

  override fun getTestRoot(): String {
    return "/move_nextjs/"
  }
  
  fun testForm() {
    doTest("pages/component.js", "pages/target")
  }

  fun testPlaceholder() {
    doTest("pages/post", "pages/test")
  }
}