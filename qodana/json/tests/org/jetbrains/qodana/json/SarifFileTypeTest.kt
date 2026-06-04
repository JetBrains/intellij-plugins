package org.jetbrains.qodana.json

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat

class SarifFileTypeTest : BasePlatformTestCase() {
  fun `test sarif file type metadata`() {
    assertThat(SarifFileType.name).isEqualTo("SARIF")
    assertThat(SarifFileType.defaultExtension).isEqualTo("sarif")
    assertThat(SarifFileType.useNativeIcon()).isFalse()
  }
}
