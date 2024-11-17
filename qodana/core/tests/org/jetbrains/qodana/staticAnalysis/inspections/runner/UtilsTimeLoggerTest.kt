package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.util.TimeoutUtil
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class UtilsTimeLoggerTest {
  @Test
  fun `time cookie is less than a second, result contains ms`() {
    val cookie = TimeCookie()
    assertTrue(cookie.formatDuration().contains("ms"))
  }

  @Test
  fun `time cookie is more than a second, result does not contains ms`() {
    val cookie = TimeCookie()
    TimeoutUtil.sleep(2000)
    assertFalse(cookie.formatDuration().contains("ms"))
    TimeoutUtil.sleep(10)
    assertFalse(cookie.formatDuration().contains("ms"))
  }
}