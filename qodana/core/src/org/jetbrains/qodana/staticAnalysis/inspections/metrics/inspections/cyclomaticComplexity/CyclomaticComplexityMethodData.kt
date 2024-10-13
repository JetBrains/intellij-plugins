package org.jetbrains.qodana.staticAnalysis.inspections.metrics.inspections.cyclomaticComplexity

data class CyclomaticComplexityMethodData(
  val methodName: String,
  val methodFileOffset: Int,
  val value: Int
)