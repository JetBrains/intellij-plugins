package org.angular2.codeInsight.inspections

import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile

class Angular2ExpressionTypesInspectionWithoutServiceTest : Angular2TestCase("inspections/expressionType", false) {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider(true))
  }

  fun testAnyType() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_CORE_8_2_14,
                      configurators = listOf(Angular2TsConfigFile()))


  fun testSlicePipe() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)


  fun testNgForOfQueryList() =
    checkHighlighting(Angular2TestModule.TS_LIB,
                      Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)

}