// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.ApplicationInfo
import java.io.File

val COMMUNITY_PRODUCT_CODES = listOf("QDJVMC", "QDPYC", "QDANDC")

fun getQodanaProductName(): String {
  val buildNumber = ApplicationInfo.getInstance().build
  return when (buildNumber.productCode) {
    "QDJVM" -> "Qodana for JVM"
    "QDJVMC" -> "Qodana Community for JVM"
    "QDJVME" -> "Qodana Enterprise for JVM"
    "QDPY" -> "Qodana for Python"
    "QDPYC" -> "Qodana Community for Python"
    "QDANDC" -> "Qodana Community for Android"
    "QDAND" -> "Qodana for Android"
    "QDJS" -> "Qodana for JS"
    "QDPHP" -> "Qodana for PHP"
    "QDGO" -> "Qodana for Go"
    "QDRST" -> "Qodana for Rust"
    "QDRUBY" -> "Qodana for Ruby"
    "QDNET" -> "Qodana for .NET"
    "QDCPP" -> "Qodana for C/C++"
    "IJCA" -> "Qodana Deprecated common"
    else -> "Qodana"
  }
}

fun splitProgressText(text: String): Pair<String, String?> {
  val splitByFileSeparator = text.split(File.separator)
  val file = if (splitByFileSeparator.size >= 2) {
    val splitForFile = splitByFileSeparator[splitByFileSeparator.size - 1].split(" ")
    splitForFile[0]
  }
  else {
    null
  }
  var forPrefix = splitByFileSeparator[0].split(" in ")
  if (forPrefix.size < 2) {
    forPrefix = splitByFileSeparator[0].split(" of ")
  }
  val prefix = forPrefix[0]
  return prefix to file
}

fun isInteractiveOutput(): Boolean {
  return System.console() != null
}