// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

@TestApplication
class QodanaStatisticsSchemaWaitTest {
  @Test
  fun `air-gapped mode does not update the schema`(): Unit = runBlocking {
    var calls = 0
    application(airGapped = true).waitForStatisticsSchemaUpdate { calls++ }
    assertEquals(0, calls)
  }

  @Test
  fun `without air-gapped mode the schema is updated`(): Unit = runBlocking {
    var calls = 0
    application(airGapped = false).waitForStatisticsSchemaUpdate { calls++ }
    assertEquals(1, calls)
  }

  private fun application(airGapped: Boolean): QodanaInspectionApplication {
    val config = QodanaConfig.fromYaml(
      Path.of(".").toAbsolutePath(),
      Path.of("unused"),
      yaml = QodanaYamlConfig(airGapped = airGapped),
      resultsStorage = Path.of("unused"),
      outputFormat = OutputFormat.SARIF_AND_PROJECT_STRUCTURE,
    )
    return QodanaInspectionApplication(config, projectApi = null)
  }
}
