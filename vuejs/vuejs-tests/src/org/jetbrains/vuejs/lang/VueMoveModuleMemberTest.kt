// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.refactoring.JSMoveModuleMembersFixture.Companion.jsMoveModuleMembersFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class VueMoveModuleMemberTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsMoveModuleMembersFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + "/refactoring/moveModuleMember/"))

  @Test
  fun testMoveBetweenVueFiles() {
    js.doTest("from.vue", "to.vue", "foo")
  }
}