package org.jetbrains.qodana.ui

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.util.application

fun getQodanaImageNameMatchingIDE(useLatestPostfix: Boolean): String {
  return "jetbrains/${getLinterName()}${if (useLatestPostfix) ":latest" else ""}"
}

private fun getLinterName(): String {
  val unknownLinter = "qodana-<linter>"
  if (application.isUnitTestMode) {
    return unknownLinter
  }
  return when (ApplicationInfo.getInstance().build.productCode) {
    "IU" -> "qodana-jvm"
    "IC" -> "qodana-jvm-community"
    "PY" -> "qodana-python"
    "PS" -> "qodana-php"
    "GO" -> "qodana-go"
    "RD" -> "qodana-dotnet"
    "WS" -> "qodana-js"
    else -> unknownLinter
  }
}