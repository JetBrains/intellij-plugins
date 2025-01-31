// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.testFramework.fixtures.EditorHintFixture
import org.angular2.Angular2TestCase

class Angular2ParameterInfoTest : Angular2TestCase("parameterInfo") {
  private lateinit var myHintFixture: EditorHintFixture

  override fun setUp() {
    super.setUp()
    myHintFixture = EditorHintFixture(testRootDisposable)
  }

  fun testPipe() =
    checkParameterInfo(extension = "ts")

}
