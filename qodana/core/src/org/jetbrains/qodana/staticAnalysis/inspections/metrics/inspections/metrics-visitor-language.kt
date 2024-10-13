package org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections

import com.intellij.codeInspection.ex.ToolLanguageUtil

enum class VisitorLanguage(val id: String) {
  KOTLIN("kotlin"),
  JAVA("JAVA")
}

fun checkLanguage(fileLanguageId: String, languageId: String): Boolean {
  if (languageId.isBlank() || languageId == "any") return true

  val languages: Set<String> = ToolLanguageUtil.getAllMatchingLanguages(languageId, true)
  return fileLanguageId in languages
}