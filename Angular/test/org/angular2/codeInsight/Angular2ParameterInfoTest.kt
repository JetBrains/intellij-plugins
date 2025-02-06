// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.testFramework.fixtures.EditorHintFixture
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.ANGULAR_COMMON_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_19_0_0_NEXT_4

class Angular2ParameterInfoTest : Angular2TestCase("parameterInfo") {
  private lateinit var myHintFixture: EditorHintFixture

  override fun setUp() {
    super.setUp()
    myHintFixture = EditorHintFixture(testRootDisposable)
  }

  fun testPipe() =
    checkParameterInfo(extension = "ts")

  fun testTemplateBindingsNgIfEmpty() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  fun testTemplateBindingsNgIf1() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  fun testTemplateBindingsNgIf2() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  fun testTemplateBindingsNgIf3() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                       extension = "ts")

  fun testForBlock1() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testForBlock2() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testForBlock3() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testIfBlock1() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testIfBlock2() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testIfBlock3() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testIfBlock4() =
    checkParameterInfo(ANGULAR_CORE_18_2_1, extension = "html")

  fun testDeferBlock1() =
    checkParameterInfo(ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

  fun testDeferBlock2() =
    checkParameterInfo(ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

  fun testDeferBlock3() =
    checkParameterInfo(ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

  fun testDeferBlock4() =
    checkParameterInfo(ANGULAR_CORE_19_0_0_NEXT_4, extension = "html")

}
