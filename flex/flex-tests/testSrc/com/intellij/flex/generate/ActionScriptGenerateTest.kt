package com.intellij.flex.generate

import com.intellij.flex.FlexTestOption
import com.intellij.flex.FlexTestOptions
import com.intellij.flex.editor.FlexProjectDescriptor
import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.generate.JSGenerateTestBase
import com.intellij.testFramework.LightProjectDescriptor
import org.jetbrains.annotations.NonNls
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ActionScriptGenerateTest : JSGenerateTestBase() {
  companion object {
    const val BASE_PATH: @NonNls String = "/js2_highlighting"
  }

  override fun setUp() {
    super.setUp()
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.testRootDisposable, "")
  }

  override fun getProjectDescriptor(): LightProjectDescriptor {
    return FlexProjectDescriptor.DESCRIPTOR
  }

  override fun getBasePath(): String {
    return BASE_PATH
  }

  override fun getTestDataPath(): String {
    return FlexTestUtils.getTestDataPath(basePath)
  }

  override fun getExtension(): String {
    return "js2"
  }

  @Test
  fun testGenerateGetter() {
    doGenerateTest("Generate.GetAccessor.JavaScript")
  }

  @Test
  fun testGenerateSetter() {
    doGenerateTest("Generate.SetAccessor.JavaScript")
  }


  @Test
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  fun testGenerateGetterAndSetter() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), javaClass, myFixture.testRootDisposable)
    doGenerateTest("Generate.GetSetAccessor.JavaScript")
  }

  @Test
  fun testGenerateConstructor() {
    doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateConstructor2() {
    doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateConstructor3() {
    doGenerateTest("Generate.Constructor.JavaScript")
  }

  @Test
  fun testGenerateToString() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2")
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2")
    doGenerateTest("Generate.ToString.Actionscript", "_3", "as")
  }

  @Test
  fun testGenerateToString2() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2")
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2")
  }

  private fun doGenerateTest(actionId: @NonNls String) {
    doGenerateTest(actionId, "js2")
  }
}
