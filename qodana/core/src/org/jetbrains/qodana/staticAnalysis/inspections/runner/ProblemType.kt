package org.jetbrains.qodana.staticAnalysis.inspections.runner

enum class ProblemType {
  REGULAR,
  TAINT,
  DUPLICATES,
  VULNERABLE_API_WITH_RELATED_LOCATIONS,
  // incorrect formatting type is for future development of a separate structural differences for this inspection
  INCORRECT_FORMATTING
}