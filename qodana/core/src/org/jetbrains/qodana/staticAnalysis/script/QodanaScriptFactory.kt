package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.ExtensionPointName.Companion.create
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory

/**
 * The configuration for a [QodanaScript] can either be specified on the command line
 * as `--script name:parameters`, or in `qodana.yaml` in the `script` section.
 *
 *
 * To parse the command line argument into a script configuration,
 * see [.parseConfigFromArgument] for the general part
 * and [.parseParameters] for parsing the script-specific parameters.
 * The result of parsing a command line argument or the section in `qodana.yaml` is
 * the [QodanaScriptConfig].
 *
 *
 * From this script configuration, [.createScript]
 * creates a [QodanaScript] that performs the actual analysis.
 */
@ApiStatus.Internal
interface QodanaScriptFactory {
  val scriptName: String

  /**
   * Validate the parameters and then create a script from them.
   *
   * @param contextFactory
   * @param parameters     either parsed from the command line (see [.parseParameters])
   * or read in from `qodana.yaml`
   * (see [QodanaYamlConfig.load][QodanaYamlConfig.Companion.load]).
   */
  fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript

  /**
   * Parse the script parameters from a single `--script` command line argument.
   * For `--script script-name`, the parameters are an empty string.
   * For `--script script-name:parameters`, the parameters are the parameters only, without the script name or the ':'.
   * The format of the parameters is specific to each script.
   *
   * @throws QodanaException if the parameters are wrong
   */
  fun parseParameters(parameters: String): Map<String, String>

  companion object {
    val EP_NAME: ExtensionPointName<QodanaScriptFactory> = create("org.intellij.qodana.qodanaScriptFactory")

    fun parseConfigFromArgument(argument: String): QodanaScriptConfig? {
      for (factory in EP_NAME.extensionList) {
        val scriptName = factory.scriptName
        if (argument.startsWith(scriptName)) {
          val afterName = argument.substring(scriptName.length)
          if (afterName.isEmpty()) {
            return QodanaScriptConfig(scriptName, factory.parseParameters(""))
          }
          if (afterName.startsWith(":")) {
            val parameters = afterName.substring(1)
            if (parameters.isEmpty()) {
              throw QodanaException("Script parameters in '--script $argument' must not be empty")
            }
            return QodanaScriptConfig(scriptName, factory.parseParameters(parameters))
          }
        }
      }
      return null
    }

    fun buildScript(
      config: QodanaConfig,
      contextFactory: QodanaRunContextFactory,
      messageReporter: QodanaMessageReporter
    ): QodanaScript {
      val name = config.script.name
      val factory = EP_NAME.extensionList.firstOrNull { it.scriptName.lowercase() == name.lowercase() }
                    ?: throw QodanaException("Script '$name' does not exist")

      val scriptName = factory.scriptName
      if (scriptName != DEFAULT_SCRIPT_NAME) {
        messageReporter.reportMessage(1, "Using '$scriptName' script as qodana run scenario")
      }

      val parameters = UnvalidatedParameters(scriptName, config.script.parameters)
      val script = factory.createScript(config, messageReporter, contextFactory, parameters)
      parameters.done()
      return script
    }
  }
}
