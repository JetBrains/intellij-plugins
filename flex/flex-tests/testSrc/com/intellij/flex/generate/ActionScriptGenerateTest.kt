package com.intellij.flex.generate

import com.intellij.flex.FlexTestOption
import com.intellij.flex.FlexTestOptions
import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.flex.FlexModuleType
import com.intellij.lang.javascript.generate.JSGenerateFixture.Companion.jsGenerateFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class ActionScriptGenerateTest {
  private val js by jsGenerateFixture(projectFixture, JSFixtureOptions(FlexTestUtils.getTestDataPath(BASE_PATH), "js2").withModuleType(FlexModuleType.MODULE_TYPE_ID))

  companion object {
    private val projectFixture = jsProjectFixture()

    const val BASE_PATH: @NonNls String = "/js2_highlighting"
  }

  @BeforeEach
  fun setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.fixture.testRootDisposable, "")
  }

  @Test
  fun testGenerateGetter() {
    js.doGenerateTest("Generate.GetAccessor.JavaScript")
  }

  @Test
  fun testGenerateSetter() {
    js.doGenerateTest("Generate.SetAccessor.JavaScript")
  }

  @Test
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  fun testGenerateGetterAndSetter() {
    FlexTestUtils.setupFlexSdk(js.fixture.module, js.getTestName(false), javaClass, js.fixture.testRootDisposable)
    js.doGenerateTest("Generate.GetSetAccessor.JavaScript")
  }

  @Test
  fun testGenerateConstructor() {
    js.doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateConstructor2() {
    js.doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateConstructor3() {
    js.doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateToString() {
    js.doGenerateTest("Generate.ToString.Actionscript", "", "js2")
    js.doGenerateTest("Generate.ToString.Actionscript", "_2", "js2")
    js.doGenerateTest("Generate.ToString.Actionscript", "_3", "as")
  }

  @Test
  fun testGenerateToString2() {
    js.doGenerateTest("Generate.ToString.Actionscript", "", "js2")
    js.doGenerateTest("Generate.ToString.Actionscript", "_2", "js2")
  }

  private fun doGenerateTest(actionId: @NonNls String) {
    js.doGenerateTest(actionId, "js2")
  }
}
