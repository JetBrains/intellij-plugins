// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@TestApplication
class QodanaStatisticsSchemaWaitTest {
  @Test
  fun `air-gapped mode does not update the schema`(): Unit = runBlocking {
    var calls = 0
    application(airGapped = true).waitForStatisticsSchemaUpdate(isSendEnabled = { true }, startUpdate = { calls++; completed() })
    assertEquals(0, calls)
  }

  @Test
  fun `without FUS sending the schema is not updated`(): Unit = runBlocking {
    var calls = 0
    application(airGapped = false).waitForStatisticsSchemaUpdate(isSendEnabled = { false }, startUpdate = { calls++; completed() })
    assertEquals(0, calls)
  }

  @Test
  fun `with FUS sending the schema is updated`(): Unit = runBlocking {
    var calls = 0
    application(airGapped = false).waitForStatisticsSchemaUpdate(isSendEnabled = { true }, startUpdate = { calls++; completed() })
    assertEquals(1, calls)
  }

  @Test
  fun `the wait ends after the timeout while the update blocks a thread`(): Unit = runBlocking {
    val release = CountDownLatch(1)
    val scope = CoroutineScope(Dispatchers.IO)
    try {
      val update = scope.async { release.await() }
      val elapsed = measureTime {
        application(airGapped = false).waitForStatisticsSchemaUpdate(timeout = 100.milliseconds, isSendEnabled = { true }, startUpdate = { update })
      }
      assertTrue(elapsed < 2.seconds, "the wait took $elapsed")
      assertTrue(update.isCancelled)
    }
    finally {
      release.countDown()
      scope.cancel()
    }
  }

  private fun completed(): Deferred<Unit> = CompletableDeferred(Unit)

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
