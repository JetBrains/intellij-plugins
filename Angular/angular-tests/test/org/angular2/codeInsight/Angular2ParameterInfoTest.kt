// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.testFramework.fixtures.EditorHintFixture
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule.ANGULAR_COMMON_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_18_2_1
import org.angular2.Angular2TestModule.ANGULAR_CORE_19_2_0
import org.angular2.Angular2TsConfigFile
import org.angular2.SkipTsGoProxy
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2ParameterInfoTest : Angular2TestCase("parameterInfo") {
  private lateinit var myHintFixture: EditorHintFixture

  override fun setUp() {
    super.setUp()
    myHintFixture = EditorHintFixture(testRootDisposable)
  }

  @Test
  @SkipTsGoProxy
  fun testPipe() =
    doParameterInfoTest(extension = "ts",
                        configurators = listOf(Angular2TsConfigFile()))

  @Test
  fun testTemplateBindingsNgIfEmpty() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                        extension = "ts",
                        configurators = listOf(Angular2TsConfigFile(strictNullChecks = false)))

  @Test
  fun testTemplateBindingsNgIf1() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                        extension = "ts",
                        configurators = listOf(Angular2TsConfigFile(strictNullChecks = false)))

  @Test
  fun testTemplateBindingsNgIf2() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                        extension = "ts",
                        configurators = listOf(Angular2TsConfigFile(strictNullChecks = false)))

  @Test
  fun testTemplateBindingsNgIf3() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, ANGULAR_COMMON_18_2_1,
                        extension = "ts",
                        configurators = listOf(Angular2TsConfigFile(strictNullChecks = false)))

  @Test
  fun testForBlock1() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testForBlock2() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testForBlock3() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testIfBlock1() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testIfBlock2() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testIfBlock3() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testIfBlock4() =
    doParameterInfoTest(ANGULAR_CORE_18_2_1, extension = "html")

  @Test
  fun testDeferBlock1() =
    doParameterInfoTest(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testDeferBlock2() =
    doParameterInfoTest(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testDeferBlock3() =
    doParameterInfoTest(ANGULAR_CORE_19_2_0, extension = "html")

  @Test
  fun testDeferBlock4() =
    doParameterInfoTest(ANGULAR_CORE_19_2_0, extension = "html")

}
