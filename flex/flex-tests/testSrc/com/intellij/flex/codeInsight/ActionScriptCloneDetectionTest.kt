// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight

import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.flex.FlexSupportLoader
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.clones.commonDuplicateDetectionFixture
import org.junit.jupiter.api.Test

@TestApplication
class ActionScriptCloneDetectionTest {

  private val duplicates by commonDuplicateDetectionFixture(FlexSupportLoader.ECMA_SCRIPT_L4, FlexTestUtils.getTestDataPath("clones/"))

  @Test
  fun testSimpleClass() {
    duplicates.doTest("SimpleClass.as", 2, true, true, false)
  }

  @Test
  fun testImplementsNormalization() {
    duplicates.doTest("ImplementsNormalization.as", 2, true, true, false)
  }
}
