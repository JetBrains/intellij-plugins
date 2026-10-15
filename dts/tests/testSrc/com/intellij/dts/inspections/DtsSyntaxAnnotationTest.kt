package com.intellij.dts.inspections

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/syntax")
class DtsSyntaxAnnotationTest : DtsInspectionTest() {
  @Test
  fun `test empty char`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test unterminated char`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test unterminated string`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test pHandle whitespace label`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test pHandle whitespace path`() = dtsTimeoutRunBlocking { doInspectionTest() }
}