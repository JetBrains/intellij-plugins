// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.pug

import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class VuePugFoldingTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testPugInTemplate() {
    myFixture.testFolding(getVueTestDataPath() + "/pug/" + hybrid.getTestName(false) + ".pug")
  }
}