package org.jetbrains.qodana.filetype

import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.filetype.QodanaSarifFileMatcher.Companion.isSarifReportFilename
import org.junit.jupiter.api.Test

@TestApplication
class SarifReportFileSupportTest {
  @Test
  fun `test sarif filename detection supports sarif patterns`() {
    assertThat(isSarifReportFilename("report.sarif")).isTrue()
    assertThat(isSarifReportFilename("report.sarif.json")).isTrue()
    assertThat(isSarifReportFilename("report.sarif.preview.json")).isTrue()
    assertThat(isSarifReportFilename("REPORT.SARIF.JSON")).isTrue()
  }

  @Test
  fun `test sarif filename detection rejects non sarif json`() {
    assertThat(isSarifReportFilename("report.json")).isFalse()
    assertThat(isSarifReportFilename("sarif-report.json")).isFalse()
    assertThat(isSarifReportFilename("report.txt")).isFalse()
  }
}
