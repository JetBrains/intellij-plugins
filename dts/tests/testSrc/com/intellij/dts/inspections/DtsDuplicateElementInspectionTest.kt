package com.intellij.dts.inspections

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/duplicateElement")
class DtsDuplicateElementInspectionTest : DtsInspectionTest(DtsDuplicateElementInspection::class) {
  @Test
  fun `test property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test mixed`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test valid`() = dtsTimeoutRunBlocking { doInspectionTest() }
}