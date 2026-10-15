package com.intellij.dts.inspections

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/statementOrder")
class DtsStatementOrderInspectionTest : DtsInspectionTest(DtsStatementOrderInspection::class) {
  @Test
  fun `test invalid property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  @Test
  fun `test invalid delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }
  @Test
  fun `test after delete node`() = dtsTimeoutRunBlocking { doInspectionTest() }
}