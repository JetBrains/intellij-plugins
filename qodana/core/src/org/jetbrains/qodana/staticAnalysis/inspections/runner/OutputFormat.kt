package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import org.jetbrains.qodana.staticAnalysis.stat.UsageCollector

internal const val QODANA_FORMAT = "qodana.format"
internal val DEFAULT_OUTPUT_FORMAT = OutputFormat.SARIF_AND_PROJECT_STRUCTURE

enum class OutputFormat {
  SARIF_AND_PROJECT_STRUCTURE,
  INSPECT_SH_FORMAT
}

internal fun getOutputFormat(): OutputFormat {
  val formatName = getOutputFormatName()

  try {
    return OutputFormat.valueOf(formatName)
  }
  catch (e: IllegalArgumentException) {
    val validValues = OutputFormat.values().joinToString { "'$it'" }
    throw QodanaException("Invalid format '$formatName' in system property '$QODANA_FORMAT'. Valid values are $validValues.")
  }
}

private fun getOutputFormatName(): String {
  val formatName = System.getProperties().getProperty(QODANA_FORMAT)

  if (formatName != null) return formatName

  //This hack should be eliminated after most of the used Teamcity qodana plugins start to provide QODANA_FORMAT as parameter
  val environment = UsageCollector.splitEnv(qodanaEnv().QODANA_ENV.value ?: "")
  return if (environment.system == "teamcity") {
    OutputFormat.INSPECT_SH_FORMAT.name
  }
  else {
    DEFAULT_OUTPUT_FORMAT.name
  }
}