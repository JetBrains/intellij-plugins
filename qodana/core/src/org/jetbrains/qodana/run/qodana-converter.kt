package org.jetbrains.qodana.run

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.io.copy
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.sarif.SarifConverter
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.exists

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
    val converterTempDir = FileUtilRt.createTempDirectory("qodana-converter", "", true).toPath()
    when (input) {
      is QodanaConverterInput.FullQodanaOutput -> {
        input.qodanaOutput.toFile().copyRecursively(converterTempDir.toFile())
      }
      is QodanaConverterInput.SarifFileOnly -> {
        input.sarifFile.toFile().copyRecursively(converterTempDir.resolve("qodana.sarif.json").toFile())
      }
    }
    val yamlFile = converterTempDir.resolve("log").resolve(QODANA_YAML_CONFIG_FILENAME)
    try {
      if (yamlFile.exists()) {
        yamlFile.copy(converterTempDir.resolve(QODANA_YAML_CONFIG_FILENAME))
      }
    }
    catch (e : IOException) {
      LOG.info("Failed copying yaml", e)
    }
    SarifConverter().convert(converterTempDir.resolve(FULL_SARIF_REPORT_NAME).toFile(), converterTempDir)

    LOG.info("Ran Qodana converter, results directory: $converterTempDir")

    QodanaConverterResults(converterTempDir)
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