package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.withContext
import org.apache.commons.cli.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.api.message
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.*
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader.defaultConfigPath
import org.jetbrains.qodana.staticAnalysis.script.CHANGES_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.DEFAULT_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptFactory
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import kotlin.io.path.Path
import kotlin.system.exitProcess

private const val QODANA_OUTPUT_LOG_LEVEL = "QODANA_OUTPUT_LOG_LEVEL"

class QodanaInspectionApplicationFactory {
  private val options = Options().apply {
    addOption("b", "baseline", true, QodanaBundle.message("baseline.option.description"))
    addOption(null, "baseline-include-absent", false, QodanaBundle.message("baseline.include.absent.option.description"))
    addOption(null, "fail-threshold", true, QodanaBundle.message("fail.threshold.option.description"))
    addOption(null, "run-promo", true, QodanaBundle.message("run.promo.inspections.option.description"))
    addOption(null, "disable-sanity", false, QodanaBundle.message("disable.sanity.inspections.option.description"))
    addOption(null, "apply-fixes", false, QodanaBundle.message("apply.fixes.inspections.option.description"))
    addOption(null, "cleanup", false, QodanaBundle.message("cleanup.inspections.option.description"))
    addOption(null, "script", true, QodanaBundle.message("script.option.description"))
    addOption(null, "config", true, QodanaBundle.message("config.option.description"))
    addOption(null, "config-dir", true, QodanaBundle.message("config.dir.option.description"))
    addOption("n", "profile-name", true, QodanaBundle.message("profileName.option.description"))
    addOption("p", "profile-path", true, QodanaBundle.message("profilePath.option.description"))
    addOption("c", "changes", false, QodanaBundle.message("changes.option.description"))
    addOption("d", "source-directory", true, QodanaBundle.message("source.directory.option.description"))
    addOption("profileName", null, true, QodanaBundle.message("profileName.deprecated.option.description"))
    addOption("profilePath", null, true, QodanaBundle.message("profilePath.deprecated.option.description"))
    addOption("changes", null, false, QodanaBundle.message("changes.deprecated.option.description"))
    addOption(null, "fixes-strategy", true, QodanaBundle.message("fixes.strategy.inspections.option.description"))
    addOption(null, "stub-profile", true, QodanaBundle.message("stubProfile.option.description"))
  }

  suspend fun getApplication(args: List<String>): QodanaInspectionApplication {
    if (args.size == 1 && args[0] == "--dump-options") {
      dumpOptions()
      exitProcess(0)
    }
    try {
      return buildApplication(args) ?: exitProcess(1)
    }
    catch (e: QodanaException) {
      throw InspectionApplicationException(e.message)
    }
  }

  private fun CommandLine.fixesStrategy(): FixesStrategy? {
    return when {
      this.hasOption("--apply-fixes") -> FixesStrategy.APPLY
      this.hasOption("--cleanup") -> FixesStrategy.CLEANUP
      this.hasOption("--fixes-strategy") -> {
        FixesStrategy.fromString(this.getOptionValue("--fixes-strategy").uppercase())
      }
      else -> null
    }
  }

  suspend fun buildApplication(args: List<String>): QodanaInspectionApplication? {
    setQodanaOutputLogLevel()
    val help = buildHelpString()
    val commandLine = parseArgs(args)
    if (commandLine == null) {
      reportError(help)
      return null
    }


    val projectPath = commandLine.args[0]
    val absoluteProjectPath = Paths.get(projectPath).toRealPath()

    val configOptionValue = commandLine.getOptionValue("config")
    val configDirOptionValue = commandLine.getOptionValue("config-dir")

    val localQodanaYaml =
      if (configOptionValue.isNullOrEmpty()) defaultConfigPath(absoluteProjectPath)
      else Path(configOptionValue).asAbsoluteFrom(absoluteProjectPath)

    if (configDirOptionValue.isNullOrEmpty()) {
      QodanaMessageReporter.DEFAULT.reportError("Config directory must be specified as 'config-dir' (computed by CLI), " +
                                                "otherwise Qodana analysis ignores global configuration and 'imports' section")
    }

    val qodanaYamlFiles = when {
      !configDirOptionValue.isNullOrEmpty() -> {
        QodanaYamlFiles.fromConfigDir(Path(configDirOptionValue).asAbsoluteFrom(absoluteProjectPath))
      }
      localQodanaYaml != null -> {
        QodanaYamlFiles.noConfigDir(localQodanaYaml)
      }
      else -> {
        QodanaYamlFiles.noFiles()
      }
    }

    val outPath = commandLine.args[1]
    val profileName = commandLine.getOptionValue("n") ?: commandLine.getOptionValue("profileName")
    val profilePath = commandLine.getOptionValue("p") ?: commandLine.getOptionValue("profilePath")
    val dirToAnalyze = commandLine.getOptionValue("d") ?: commandLine.getOptionValue("source-directory")

    val effectiveQodanaYamlPath = qodanaYamlFiles.effectiveQodanaYaml
    @Suppress("DEPRECATION") val yamlConfig = effectiveQodanaYamlPath?.let { effectiveConfig ->
      withContext(StaticAnalysisDispatchers.IO) {
        QodanaYamlReader.load(effectiveConfig)
      }
    }?.getOrThrow()?.withAbsoluteProfilePath(absoluteProjectPath, effectiveQodanaYamlPath) ?: QodanaYamlConfig.EMPTY_V1
    val runPromo = commandLine.getOptionValue("run-promo")?.toBoolean() ?: yamlConfig.runPromoInspections
    val disableSanity = commandLine.hasOption("disable-sanity") || yamlConfig.disableSanityInspections
    val failThresholdArg = commandLine.getOptionValue("fail-threshold")?.toInt()

    val script = determineScript(commandLine, yamlConfig)

    val includeAbsent = commandLine.hasOption("baseline-include-absent") || yamlConfig.includeAbsent

    val profile = yamlConfig.profile.copy(
      name = profileName?.takeUnless(String::isBlank) ?: yamlConfig.profile.name,
      path = profilePath?.takeUnless(String::isBlank) ?: yamlConfig.profile.path
    )
    val qodanaConfig = QodanaConfig.fromYaml(
      absoluteProjectPath,
      Paths.get(outPath),
      yamlFiles = qodanaYamlFiles,
      yaml = yamlConfig,
      profile = profile,
      baseline = commandLine.getOptionValue("baseline"),
      disableSanityInspections = disableSanity,
      fixesStrategy = commandLine.fixesStrategy() ?: FixesStrategy.fromString(yamlConfig.fixesStrategy.uppercase()),
      runPromoInspections = runPromo,
      script = script,
      includeAbsent = includeAbsent,
      sourceDirectory = dirToAnalyze,
      failureConditions = if (failThresholdArg == null) {
        yamlConfig.failureConditions
      }
      else {
        yamlConfig.failureConditions.copy(severityThresholds = yamlConfig.failureConditions.severityThresholds.copy(any = failThresholdArg))
      }
    )
    val projectApiResponse = obtainQodanaCloudProjectApi()
    val projectApi = when(projectApiResponse) {
      null -> null
      is QDCloudResponse.Success -> {
        projectApiResponse.value
      }
      is QDCloudResponse.Error -> {
        reportError("Cannot connect to Qodana Cloud API")
        reportError(projectApiResponse.message)
        reportError(projectApiResponse.exception)
        return null
      }
    }
    return QodanaInspectionApplication(qodanaConfig, projectApi)
  }

  private fun determineScript(cli: CommandLine, config: QodanaYamlConfig): QodanaScriptConfig {
    val cliScript: QodanaScriptConfig? = cli.getOptionValue("script")?.let {
      val parsedConfig = QodanaScriptFactory.parseConfigFromArgument(it)
      if (parsedConfig == null) {
        throw QodanaException("Can't find script implementation for '$it' value")
      }
      parsedConfig
    }
    val hasDeprecatedCliFlag = cli.hasOption("c") || cli.hasOption("changes")
    val fromConfig = config.script

    // null/default would be overridden, changes is just redundant
    val allowedScriptNames = hashSetOf(null, DEFAULT_SCRIPT_NAME, CHANGES_SCRIPT_NAME)

    return when {
      !hasDeprecatedCliFlag -> cliScript ?: config.script
      cliScript?.name !in allowedScriptNames -> {
        throw QodanaException(
          "Cannot combine '--script' option with '--changes'. Consider using '--script local-changes' instead of '--changes'")
      }
      fromConfig.name !in allowedScriptNames -> {
        throw QodanaException("Cannot combine '--changes' option with configured script '${fromConfig.name}' in yaml." +
                              "Consider using '--script local-changes' to overwrite the yaml configuration.")
      }
      else -> {
        QodanaMessageReporter.DEFAULT.reportMessage(1, "Consider using '--script $CHANGES_SCRIPT_NAME' or configuring it via yaml, " +
                                                       "instead of using '--changes'")
        QodanaScriptConfig(CHANGES_SCRIPT_NAME)
      }
    }
  }

  private fun buildHelpString(): String {
    val sw = StringWriter()
    val formatter = HelpFormatter()
    formatter.optionComparator = deprecatedLast(formatter.optionComparator)
    formatter.printHelp(
      PrintWriter(sw),
      formatter.width,
      QodanaBundle.message("usage.help.description"),
      "",
      options,
      formatter.leftPadding,
      formatter.descPadding,
      "")
    return sw.toString()
  }

  private fun dumpOptions() {
    val formatter = HelpFormatter()
    formatter.syntaxPrefix = "Qodana linter options:"
    formatter.optionComparator = deprecatedLast(formatter.optionComparator)
    System.out.writer().use { writer ->
      formatter.printHelp(
        PrintWriter(writer),
        formatter.width,
        " ",
        "",
        options,
        formatter.leftPadding,
        formatter.descPadding,
        "")
    }
  }


  private fun parseArgs(args: List<String>): CommandLine? {
    try {
      val commandLine = DefaultParser().parse(options, args.toTypedArray(), true)
      if (commandLine.argList.size != 2) {
        throw QodanaException("Arguments should contain only PROJECT_PATH and RESULTS_PATH. Arguments: " + commandLine.argList)
      }
      return commandLine
    }
    catch (e: ParseException) {
      throw QodanaException(e.message!!)
    }
  }

  private fun reportError(message: String) {
    QodanaMessageReporter.DEFAULT.reportError(message)
  }

  private fun reportError(e: Exception) {
    QodanaMessageReporter.DEFAULT.reportError(e)
  }

  private fun deprecatedLast(default: Comparator<Option>) =
    Comparator.comparing { o: Option -> o.description.startsWith("Deprecated:") }
      .thenComparing(default)

  private fun setQodanaOutputLogLevel() {
    val rootLogger = java.util.logging.Logger.getLogger("")
    val level = System.getenv(QODANA_OUTPUT_LOG_LEVEL)?.let {
      try {
        Level.parse(it)
      }
      catch (_: IllegalArgumentException) {
        LOG.warn("Can't parse log level $it")
        null
      }
    } ?: Level.SEVERE
    for (h in rootLogger.handlers) {
      if (h is ConsoleHandler) {
        h.level = level
      }
    }
  }

  companion object {
    private val LOG = logger<QodanaInspectionApplicationFactory>()
  }
}

private fun Path.asAbsoluteFrom(other: Path): Path {
  return if (isAbsolute) this else other.resolve(this).toAbsolutePath()
}
