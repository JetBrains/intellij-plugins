package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSMoveTestBase

open class NextJsMoveTest: JSMoveTestBase()  {

  override fun getTestDataPath(): String {
    return NextJsTestUtil.getTestDataPath()
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