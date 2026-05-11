package org.jetbrains.qodana.filetype

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.filetype.QodanaSarifFileMatcher.Companion.isSarifReportFilename

class SarifReportFileSupportTest : QodanaPluginLightTestBase() {
  fun `test sarif filename detection supports sarif patterns`() {
    assertThat(isSarifReportFilename("report.sarif")).isTrue()
    assertThat(isSarifReportFilename("report.sarif.json")).isTrue()
    assertThat(isSarifReportFilename("report.sarif.preview.json")).isTrue()
    assertThat(isSarifReportFilename("REPORT.SARIF.JSON")).isTrue()
  }

  fun `test sarif filename detection rejects non sarif json`() {
    assertThat(isSarifReportFilename("report.json")).isFalse()
    assertThat(isSarifReportFilename("sarif-report.json")).isFalse()
    assertThat(isSarifReportFilename("report.txt")).isFalse()
  }
}
