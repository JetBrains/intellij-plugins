package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaProjectLoader
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Test
import kotlin.io.path.Path

class QodanaProjectLoaderTest : QodanaTestCase() {
  @Test
  fun `open project should fail with configuration error when project path does not exist`() = runTest {
    val config = QodanaConfig.fromYaml(
      Path("/absolutely/does/not/exist"),
      Path("/output/is/ignored"),
      resultsStorage = Path("/yet/another/ignored/path"),
      outputFormat = OutputFormat.SARIF_AND_PROJECT_STRUCTURE
    )

    val error = runCatching { QodanaProjectLoader(QodanaMessageReporter.EMPTY).openProject(config) }.exceptionOrNull()

    assertThat(error).isExactlyInstanceOf(QodanaConfigurationException::class.java)
  }
}
