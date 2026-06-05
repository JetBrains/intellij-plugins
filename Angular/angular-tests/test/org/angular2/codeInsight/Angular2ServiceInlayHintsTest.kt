package org.angular2.codeInsight

import com.intellij.lang.typescript.editing.TypeScriptServiceInlayHintsService
import com.intellij.lang.typescript.service.configureJSInlayHints
import com.intellij.lang.typescript.service.testTSServiceInlayHints
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.SkipTsGoFork
import org.angular2.TestTsGoFork
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoFork
class Angular2ServiceInlayHintsTest : Angular2TestCase("inlayHints") {

  @Test
  @SkipTsGoFork
  fun testBasic() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  @Test
  @SkipTsGoFork
  fun testExternalTemplate() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    dir = true,
    extension = "html",
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  @Test
  @SkipTsGoFork
  fun testPipeExternalTemplate() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    dir = true,
    extension = "html",
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  @Test
  @SkipTsGoFork
  fun testPipe() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  @Test
  @SkipTsGoFork
  fun testAnyCalls() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    checkInlayHints()
  }

  private fun checkInlayHints() {
    TypeScriptServiceInlayHintsService.testInlayHints(testRootDisposable)
    myFixture.configureJSInlayHints(
      showForShortHandFunctionsOnlyParamTypes = true,
      showForFunctionReturnTypes = true,
      showForParamTypes = true,
      showForLiteralParamNames = true,
      showForNonLiteralParamNames = true,
      showForVariablesAndFieldsTypes = true,
    )
    myFixture.testTSServiceInlayHints()
  }
}