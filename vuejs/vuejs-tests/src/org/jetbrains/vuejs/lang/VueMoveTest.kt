// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSMoveFixture.jsMoveFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class VueMoveTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsMoveFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + "/refactoring/move/"))

  @Test
  fun testTsPathMappings() {
    js.doTest("src/components/ImportedFile.vue", "src/components/sub-directory")
  }

  @Test
  fun testIndexFile() {
    js.doTest("declared/subdir/index.vue", "declared")
  }
}