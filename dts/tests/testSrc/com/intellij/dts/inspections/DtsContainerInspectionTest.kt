package com.intellij.dts.inspections

import com.intellij.dts.core.inspections.DtsContainerInspection
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/inspections/container")
class DtsContainerInspectionTest : DtsInspectionTest(DtsContainerInspection::class, needsZephyr = false, extension = "dts") {
  @Test
  fun `test root property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test root sub node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test root delete property`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test root delete node by name`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test root delete node by ref`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test node root node`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test node v1`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test node delete node by name`() = dtsTimeoutRunBlocking { doInspectionTest() }

  @Test
  fun `test node delete node by ref`() = dtsTimeoutRunBlocking { doInspectionTest() }
}