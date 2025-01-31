// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.testFramework.fixtures.EditorHintFixture
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_COMMON_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_18_2_1

class Angular2ParameterInfoTest : Angular2TestCase("parameterInfo") {
  private lateinit var myHintFixture: EditorHintFixture

  override fun setUp() {
    super.setUp()
    myHintFixture = EditorHintFixture(testRootDisposable)
  }

  fun testPipe() =
    checkParameterInfo(extension = "ts")

  // TODO fails on server
  fun _testTemplateBindingsNgIfEmpty() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  // TODO fails on server
  fun _testTemplateBindingsNgIf1() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  // TODO fails on server
  fun _testTemplateBindingsNgIf2() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  // TODO fails on server
  fun _testTemplateBindingsNgIf3() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

}
