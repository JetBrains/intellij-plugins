// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture.jsIntroduceVariableFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class VueIntroduceVariableTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsIntroduceVariableFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + "/refactoring/introduceVar/"))

  @Test
  fun testExpandArrowBody() {
    js.doTest("created", true, ".vue")
  }
}