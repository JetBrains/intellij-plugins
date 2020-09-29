// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableTestCase

class VueIntroduceVariableTest : JSIntroduceVariableTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/refactoring/introduceVar/"

  fun testExpandArrowBody() {
    doTest("created", true, ".vue")
  }
}