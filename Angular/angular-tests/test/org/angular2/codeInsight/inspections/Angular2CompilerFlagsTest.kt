// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile

class Angular2CompilerFlagsTest : Angular2TestCase("inspections/compilerFlags", true) {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
  }

  // TODO WEB-67260 - fix issues with Angular flags
  fun _testStrictNullInputTypesOn() {
    doFlagTest(Angular2TsConfigFile(strictNullInputTypes = true, strictNullChecks = true))
  }

  fun testStrictNullInputTypesOnStrictNullChecksOff() {
    doFlagTest(Angular2TsConfigFile(strictNullInputTypes = true, strictNullChecks = false))
  }

  // TODO WEB-67260 - fix issues with Angular flags
  fun _testStrictNullInputTypesOff() {
    doFlagTest(Angular2TsConfigFile(strictNullInputTypes = false, strictTemplates = false, strictNullChecks = true))
  }

  // TODO WEB-67260 - fix issues with Angular flags
  fun _testStrictNullInputTypesOnStrictTemplatesOff() {
    doFlagTest(Angular2TsConfigFile(strictNullInputTypes = true, strictTemplates = false, strictNullChecks = true))
  }

  // TODO WEB-67260 - fix issues with Angular flags
  fun _testStrictNullInputTypesAbsentOn() {
    doFlagTest(Angular2TsConfigFile(strictNullChecks = true, strictTemplates = true))
  }

  // TODO WEB-67260 - fix issues with Angular flags
  fun _testStrictInputAccessModifiersOn() {
    doFlagTest(Angular2TsConfigFile(strictTemplates = false, strictInputAccessModifiers = true))
  }

  fun testStrictInputAccessModifiersOff() {
    doFlagTest(Angular2TsConfigFile(strictTemplates = true, strictInputAccessModifiers = false))
  }

  fun testStrictInputAccessModifiersAbsent() {
    doFlagTest(Angular2TsConfigFile(strictTemplates = true, strictInputAccessModifiers = null))
  }

  private fun doFlagTest(test: Angular2TsConfigFile) {
    doConfiguredTest(
      Angular2TestModule.TS_LIB,
      Angular2TestModule.ANGULAR_CORE_16_2_8,
      Angular2TestModule.ANGULAR_COMMON_16_2_8,
      extension = "ts", configurators = listOf(test)
    ) {
      checkHighlighting()
    }
  }

}