package org.angular2.codeInsight.inspections

import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2ExpressionTypesInspectionWithoutServiceTest : Angular2TestCase("inspections/expressionType") {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider(true))
  }

  @Test
  fun testAnyType() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_CORE_8_2_14,
                       configurators = listOf(Angular2TsConfigFile()))


  @Test
  fun testSlicePipe() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)


  @Test
  fun testNgForOfQueryList() =
    doHighlightingTest(Angular2TestModule.TS_LIB,
                       Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)

}