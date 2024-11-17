package org.jetbrains.qodana.php

import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalFlowOutputConsumer

class PhpGlobalOutputConsumer : GlobalFlowOutputConsumer() {
  override fun getInspectionName() = "PhpVulnerablePathsInspection"
}