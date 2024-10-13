package org.jetbrains.qodana.staticAnalysis.script

enum class AnalysisKind(val stringPresentation: String) {
  REGULAR("regular"),
  INCREMENTAL("incremental"),
  OTHER("other"),
  IDE("ide")
}