package org.jetbrains.qodana.staticAnalysis.inspections.runner.log

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.MemoryDumpHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.TimeCookie
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

private val LOG = logger<QodanaMemorySnapshotLoggingActivity>()

private class QodanaMemorySnapshotLoggingActivity : QodanaLoggingActivity {
  override suspend fun executeActivity(progressName: String, timeCookie: TimeCookie) {
    val delayMinutes = System.getProperty("qodana.hprof.period", "-1").toInt()

    val isEnabled = delayMinutes > 0
    if (!isEnabled) {
      return
    }

    LOG.info("${progressName}| Memory snapshot .hprof collection is enabled with period of $delayMinutes minutes")

    while (true) {
      delay(delayMinutes.minutes)
      val snapshotDate = SimpleDateFormat("dd.MM.yyyy_HH.mm.ss").format(Date())
      val file = Path(PathManager.getLogPath(), "qodana_memory_$snapshotDate.zip")
      try {
        LOG.info("${progressName}| Capturing memory snapshot: $file")
        withContext(QodanaDispatchers.IO) {
          MemoryDumpHelper.captureMemoryDumpZipped(file)
        }
      }
      catch (ce : CancellationException) {
        throw ce
      }
      catch (e: Exception) {
        LOG.warn("${progressName}| Failed to capture memory snapshot", e)
      }
    }
  }
}