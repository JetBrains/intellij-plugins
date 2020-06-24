// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueCommenterTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/commenter/"

  fun testCss() = doTest()
  fun testSass() = doTest()
  fun testScss() = doTest()
  fun testLess() = doTest()
  fun testStylus() = doTest()

  fun doTest() {
    val name = getTestName(true)
    myFixture.configureByFile("$name.vue")
    myFixture.performEditorAction("CommentByLineComment")
    myFixture.checkResultByFile("${name}_after.vue")
  }

}
