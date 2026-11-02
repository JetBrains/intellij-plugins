package org.jetbrains.qodana.json

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SarifFileTypeTest {
  @Test
  fun `test sarif file type metadata`() {
    assertThat(SarifFileType.name).isEqualTo("SARIF")
    assertThat(SarifFileType.defaultExtension).isEqualTo("sarif")
    assertThat(SarifFileType.useNativeIcon()).isFalse()
  }
}
