package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

internal class OpenGrepGlobalInspectionOutputConsumer: GlobalTraceFlowOutputConsumer() {
  override fun getInspectionName(): String {
    return "OpenGrepGlobalInspection"
  }
}
