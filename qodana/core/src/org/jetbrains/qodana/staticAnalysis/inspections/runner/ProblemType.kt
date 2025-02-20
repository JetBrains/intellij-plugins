package org.jetbrains.qodana.staticAnalysis.inspections.runner

enum class ProblemType {
  REGULAR,
  TAINT,
  DUPLICATES,
  VULNERABLE_API_WITH_RELATED_LOCATIONS,
  INCORRECT_FORMATTING
}