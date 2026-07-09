package org.angular2.codeInsight

import com.intellij.lang.typescript.service.configureJSInlayHints
import com.intellij.lang.typescript.service.testTSServiceInlayHints
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2ServiceInlayHintsTest : Angular2TestCase("inlayHints") {

  @Test
  fun testBasic() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    testTSServiceInlayHints()
  }

  @Test
  fun testExternalTemplate() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    dir = true,
    extension = "html",
    configurators = listOf(Angular2TsConfigFile())
  ) {
    testTSServiceInlayHints()
  }

  @Test
  fun testPipeExternalTemplate() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    dir = true,
    extension = "html",
    configurators = listOf(Angular2TsConfigFile())
  ) {
    testTSServiceInlayHints()
  }

  @Test
  fun testPipe() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    testTSServiceInlayHints()
  }

  @Test
  fun testAnyCalls() = doConfiguredTest(
    Angular2TestModule.ANGULAR_CORE_18_2_1, Angular2TestModule.ANGULAR_COMMON_18_2_1,
    configurators = listOf(Angular2TsConfigFile())
  ) {
    testTSServiceInlayHints()
  }

  override fun setUp() {
    super.setUp()
    myFixture.configureJSInlayHints(
      showForShortHandFunctionsOnlyParamTypes = true,
      showForFunctionReturnTypes = true,
      showForParamTypes = true,
      showForLiteralParamNames = true,
      showForNonLiteralParamNames = true,
      showForVariablesAndFieldsTypes = true,
    )
  }
}