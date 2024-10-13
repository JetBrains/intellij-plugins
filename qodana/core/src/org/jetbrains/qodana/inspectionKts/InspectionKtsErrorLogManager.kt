package org.jetbrains.qodana.inspectionKts

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.objectTree.ThrowableInterner
import kotlinx.coroutines.*
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import java.io.FileWriter
import java.io.PrintWriter
import java.lang.Exception
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*

internal class InspectionKtsErrorLogManager(scope: CoroutineScope) {
  private val logFile: Deferred<Path> = scope.async(StaticAnalysisDispatchers.IO, start = CoroutineStart.LAZY) {
    runInterruptible {
      val logDir = PathManager.getLogDir()
      FileUtil.createTempFile(logDir.toFile(), "inspection-kts-", ".log", true).toPath()
    }
  }

  private val exceptionHeader: String = "=".repeat(100)

  data class LogFileLocation(val file: Path, val line: Int)

  interface ErrorInLogProvider {

    suspend fun loggedExceptionLocation(exception: Exception): LogFileLocation?
  }

  inner class Logger(private val inspectionKtsFile: Path) : ErrorInLogProvider {
    private val alreadyLoggedExceptionIds = ConcurrentHashMap<String, Unit>()

    suspend fun logException(exception: Exception) {
      val exceptionId = exceptionId(exception)
      val alreadyLogged = alreadyLoggedExceptionIds.putIfAbsent(exceptionId, Unit) != null
      if (alreadyLogged) {
        thisLogger().info("Already logged exception $exceptionId")
        return
      }

      val logFile = logFile.await()
      runInterruptible(StaticAnalysisDispatchers.IO) {
        logFile.appendLines(
          listOfNotNull(
            exceptionHeader,
            "${Instant.now()} | $exceptionId",
            exception.message
          )
        )
        FileWriter(logFile.toFile(), true).use {
          PrintWriter(it, true).use { writer ->
            exception.printStackTrace(writer)
          }
        }
      }
      thisLogger().info("Logged exception $exceptionId")
    }

    override suspend fun loggedExceptionLocation(exception: Exception): LogFileLocation? {
      val exceptionId = exceptionId(exception)
      thisLogger().info("Searching for exception $exceptionId")
      var line = 0
      val file = logFile.await()
      return runInterruptible(StaticAnalysisDispatchers.IO) {
        file.forEachLine { lineContent ->
          if (lineContent.endsWith(exceptionId)) {
            return@runInterruptible LogFileLocation(file, line)
          }
          line += 1
        }
        thisLogger().info("Can't find in log exception $exceptionId")
        return@runInterruptible null
      }
    }

    private fun exceptionId(exception: Exception): String {
      val exceptionHash = ThrowableInterner.computeAccurateTraceHashCode(exception)
      val loggerHash = System.identityHashCode(this)
      return "Error in $inspectionKtsFile Inspection, errorId $exceptionHash $loggerHash"
    }
  }
}