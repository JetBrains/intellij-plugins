package org.jetbrains.qodana.ui

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.util.application

fun getQodanaImageNameMatchingIDE(useVersionPostfix: Boolean, linter: Linter = getLinter()): String {
  val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
  val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart
  return "jetbrains/${linter.imageName}${if (useVersionPostfix) ":${ideMajorVersion}.${ideMinorVersion}" else ""}"
}

private fun getLinter(): Linter {
  if (application.isUnitTestMode) {
    return Linter.UNKNOWN
  }
  return Linter.entries.find { linter ->
    linter.name == ApplicationInfo.getInstance().build.productCode
  } ?: Linter.UNKNOWN
}

enum class Linter(val imageName: String) {
  IU("qodana-jvm"),
  IC("qodana-jvm-community"),
  PY("qodana-python"),
  PS("qodana-php"),
  GO("qodana-go"),
  RD("qodana-dotnet"),
  WS("qodana-js"),
  UNKNOWN("qodana-<linter>")
}