package org.jetbrains.qodana.jvm.java

import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalFlowOutputConsumer

class JvmGlobalTaintOutputConsumer: GlobalFlowOutputConsumer() {
  override fun getInspectionName() = "JvmTaintAnalysis.Global"
}