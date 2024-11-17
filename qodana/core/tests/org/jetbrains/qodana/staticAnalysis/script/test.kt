package org.jetbrains.qodana.staticAnalysis.script

import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import kotlin.io.path.Path

fun buildScript(scriptName: String, vararg scriptParameters: Pair<String, String>): QodanaScript {
  val yaml = QodanaYamlConfig.EMPTY_V1.copy(script = QodanaScriptConfig(scriptName, scriptParameters.toMap()))
  val config = QodanaConfig.fromYaml(Path("/ignored"), Path("/ignored"), yaml = yaml)
  return QodanaScriptFactory.buildScript(config, { error("No call expected!") }, QodanaMessageReporter.EMPTY)
}
