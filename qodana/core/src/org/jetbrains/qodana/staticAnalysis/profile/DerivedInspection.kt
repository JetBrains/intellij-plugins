package org.jetbrains.qodana.staticAnalysis.profile

data class DerivedInspection(
  val id: String,
  val parentInspectionId: String,
  val displayName: String,
  val description: String,
)
