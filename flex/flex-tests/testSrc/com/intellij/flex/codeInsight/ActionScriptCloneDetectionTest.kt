// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight

import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.Language
import com.intellij.lang.javascript.flex.FlexSupportLoader
import com.jetbrains.clones.CommonDuplicateTest

class ActionScriptCloneDetectionTest : CommonDuplicateTest() {

  override val language: Language get() = FlexSupportLoader.ECMA_SCRIPT_L4

  override fun getTestDataPath(): String = FlexTestUtils.getTestDataPath("clones/")

  fun testSimpleClass() {
    doTest("SimpleClass.as", 2, true, true, false)
  }

  fun testImplementsNormalization() {
    doTest("ImplementsNormalization.as", 2, true, true, false)
  }
}
