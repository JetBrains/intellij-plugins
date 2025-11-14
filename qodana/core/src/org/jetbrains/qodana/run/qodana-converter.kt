package org.jetbrains.qodana.run

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.io.copyRecursively
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.sarif.QodanaReportConverter
import java.io.IOException
import java.nio.file.Path

private val LOG = Logger.getInstance("#org.jetbrains.qodana.run")

sealed interface QodanaConverterInput {
  class SarifFileOnly(val sarifFile: Path) : QodanaConverterInput

  class FullQodanaOutput(val qodanaOutput: Path) : QodanaConverterInput
}

data class QodanaConverterResults(val path: Path) {
  fun getOutputFile(path: String): Path? = this.path.getChildByRelativePath(path)
}

suspend fun runQodanaConverter(input: QodanaConverterInput): QodanaConverterResults {
  return runInterruptible(QodanaDispatchers.IO) {
    val converterOutTempDir = FileUtilRt.createTempDirectory("qodana-converter", "", true).toPath()
    val converterInputTempDir = FileUtilRt.createTempDirectory("qodana-converter-input", "", true).toPath()
    when (input) {
      is QodanaConverterInput.FullQodanaOutput -> {
        input.qodanaOutput.copyRecursively(converterInputTempDir)
      }
      is QodanaConverterInput.SarifFileOnly -> {
        input.sarifFile.copyRecursively(converterInputTempDir.resolve("qodana.sarif.json"))
      }
    }
    val options = QodanaReportConverter.Options(Int.MAX_VALUE, converterInputTempDir.toFile(), converterOutTempDir.toFile())
    QodanaReportConverter(options).convert()

    LOG.info("Ran Qodana converter, results directory: $converterOutTempDir")

    QodanaConverterResults(converterOutTempDir)
  }
}

private fun Path.getChildByRelativePath(childRelativePath: String): Path? {
  try {
    val childFullPath = resolve(childRelativePath).toAbsolutePath().normalize()
    return if (childFullPath.startsWith(this.toAbsolutePath().normalize())) childFullPath else null
  }
  catch (_: IOException) {
    LOG.warn("Failed to load child file: $childRelativePath, parent $this")
    return null
  }
}