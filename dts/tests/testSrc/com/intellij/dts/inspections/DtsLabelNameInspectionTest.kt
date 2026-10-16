package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsLabelNameInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/labelName")
class DtsLabelNameInspectionTest : DtsInspectionTest(DtsLabelNameInspection::class) {
  @Test
  fun `test invalid start`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test invalid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}