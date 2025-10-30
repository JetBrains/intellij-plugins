package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.events.EventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.Strings
import com.intellij.openapi.vfs.readText
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.license.QodanaLicense
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.inspections.config.FailureConditions
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.script.DEFAULT_SCRIPT_NAME
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.github.DefaultQodanaGithubWorkflowBuilder
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import java.io.IOException
import java.time.Duration
import java.time.Instant

object UsageCollector : CounterUsagesCollector() {
  private val GROUP = QodanaEventLogGroup("qodana.usage", 14)

  override fun getGroup() = GROUP.eventLogGroup

  private val knownSystems = listOf(  // from https://github.com/cucumber/ci-environment/blob/main/CiEnvironments.json
    "azure-pipelines",
    "bamboo",
    "buddy",
    "bitrise",
    "circleci",
    "codefresh",
    "codeship",
    "github-actions",
    "gitlab",
    "gocd",
    "jenkins",
    "jetbrains-space",
    "semaphore",
    "travis-ci",
    "wercker",
    "cli",
    "teamcity",
    "teamcity-cloud",
    "gradle",
    "other"
  )

  private val systemField = EventFields.String("system", knownSystems)
  private val versionField = EventFields.StringValidatedByInlineRegexp("version", "(?x) \\d+ (?:\\.\\d+)* (?:_EAP)?")
  private val buildField = EventFields.StringValidatedByRegexpReference("build", "integer")

  private val envEvent = GROUP.registerVarargEvent(
    "env",
    systemField,
    versionField,
    buildField,
  )

  private val knownProfiles = listOf(
    "empty",       // The predefined empty profile, containing no inspections.
    "starter",     // The predefined starter profile, for a first-time impression.
    "recommended", // The predefined recommended profile, curated by the Qodana team.
    "single",      // An ad-hoc profile that runs a single inspection; not officially documented.
    "path",        // A user-specified profile from the project, identified via its path.
    "absent",      // No explicit profile was specified in the command line or in qodana.yaml.
    "other",       // Anything else, such as a user-specified profile identified by name.
  )

  private val profileField = EventFields.String("profile", knownProfiles)
  private val includeField = EventFields.RoundedInt("include")
  private val excludeField = EventFields.RoundedInt("exclude")
  private val stopThresholdField = EventFields.RoundedInt("stopThreshold")
  private val bootstrapField = EventFields.Boolean("bootstrap")
  private val scriptField = EventFields.String("script", listOf("php-migration", "local-changes", "migrate-classes", "scoped", "other"))
  private val phpMigrationFromLevelField = EventFields.StringValidatedByRegexpReference("phpMigrationFromLevel", "version")
  private val phpMigrationToLevelField = EventFields.StringValidatedByRegexpReference("phpMigrationToLevel", "version")

  private val failureConditionAnySeverityField = EventFields.RoundedInt("failureConditionAnySeverity")
  private val failureConditionCriticalField = EventFields.RoundedInt("failureConditionCritical")
  private val failureConditionHighField = EventFields.RoundedInt("failureConditionHigh")
  private val failureConditionModerateField = EventFields.RoundedInt("failureConditionModerate")
  private val failureConditionLowField = EventFields.RoundedInt("failureConditionLow")
  private val failureConditionInfoField = EventFields.RoundedInt("failureConditionInfo")
  private val failureConditionMinimumTotalCoverageField = EventFields.RoundedInt("failureConditionMinimumTotalCoverage")
  private val failureConditionMinimumFreshCoverageField = EventFields.RoundedInt("failureConditionMinimumFreshCoverage")

  private val onlyDirectoryField = EventFields.Boolean("onlyDirectory")
  private val includeAbsentField = EventFields.Boolean("includeAbsent")
  private val fixesStrategyField = EventFields.Enum<FixesStrategy>("fixesStrategy") { it.name.lowercase() }
  private val baselineField = EventFields.String("baselineType", listOf("none", "local", "cloud"))

  private val configEvent = GROUP.registerVarargEvent(
    "config",
    profileField,
    includeField,
    excludeField,
    stopThresholdField,
    bootstrapField,
    scriptField,
    phpMigrationFromLevelField,
    phpMigrationToLevelField,
    failureConditionAnySeverityField,
    failureConditionCriticalField,
    failureConditionHighField,
    failureConditionModerateField,
    failureConditionLowField,
    failureConditionInfoField,
    failureConditionMinimumTotalCoverageField,
    failureConditionMinimumFreshCoverageField,
    onlyDirectoryField,
    includeAbsentField,
    fixesStrategyField,
    baselineField
  )

  private val licenseTypeField = EventFields.Enum("licenseType", QodanaLicenseType::class.java)
  private val trialField = EventFields.Boolean("trial")
  private val daysLeftField = EventFields.RoundedInt("daysLeft")


  private val licenseEvent = GROUP.registerVarargEvent(
    "license.info",
    licenseTypeField,
    trialField,
    daysLeftField
  )

  private val qodanaYamlDetectedEvent = GROUP.registerVarargEvent(
    "qodana.yaml.detected"
  )

  enum class QodanaConfigSource {
    LOCAL,
    GLOBAL,
    LOCAL_AND_GLOBAL,

    UNKNOWN
  }

  private val QODANA_CONFIG_SOURCE = EventFields.Enum<QodanaConfigSource>("configSource")

  private val qodanaConfigurationSourceEvent = GROUP.registerVarargEvent(
    "qodana.configuration.source",
    QODANA_CONFIG_SOURCE
  )


  private val qodanaGithubPromoWorkflowUsed = GROUP.registerVarargEvent(
    "qodana.github.promo.workflow.used"
  )

  internal data class Environment(val system: String, val version: String?, val build: String?)

  internal fun splitEnv(env: String): Environment {
    val regex = """(?x)
        ([\w-]+)                          # system
        (?: : (\d+ (?:\.\d+)* (?:_EAP)?)  # optional version; keep in sync with versionField
          (?: : ([1-9]\d{0,8}) )?         # optional build
        )?
      """
    Regex(regex).matchEntire(env)?.let { m ->
      val (system, version, build) = m.destructured
      if (system == "teamcity" || system == "teamcity-cloud")
        return Environment(system, Strings.nullize(version), Strings.nullize(build))
      if (system in knownSystems)
        return Environment(system, Strings.nullize(version), null)
    }
    return Environment("other", null, null)
  }

  fun logQodanaYamlPresent() {
    qodanaYamlDetectedEvent.log()
  }

  fun logQodanaConfigSource(source: QodanaConfigSource) {
    qodanaConfigurationSourceEvent.log(QODANA_CONFIG_SOURCE with source)
  }

  @JvmStatic
  fun logEnv(qodanaEnv: String?) {
    val env = splitEnv(qodanaEnv ?: "other")
    val args = mutableListOf<EventPair<*>>()
    args += systemField with env.system
    if (env.version != null)
      args += versionField with env.version
    if (env.build != null)
      args += buildField with env.build
    envEvent.log(args)
  }

  /**
   * Classify the kind of profile that has been configured by the Qodana user,
   * either via the command line options or from `qodana.yaml`.
   *
   * * [Qodana documentation](https://www.jetbrains.com/help/qodana/qodana-yaml.html#Default+profiles)
   * * [Predefined profiles](https://github.com/JetBrains/qodana-profiles/tree/master/.idea/inspectionProfiles)
   */
  fun profileForReporting(name: String, path: String) = when {
    name == "empty" -> "empty"
    name == "qodana.starter" -> "starter"
    name == "qodana.recommended" -> "recommended"
    name.startsWith("qodana.single:") -> "single"
    name == "" && path != "" -> "path"
    name == "" && path == "" -> "absent"
    else -> "other"
  }

  @JvmStatic
  fun logConfig(config: QodanaConfig, profileName: String, profilePath: String) {
    val args = mutableListOf<EventPair<*>>()
    args += profileField with profileForReporting(profileName, profilePath)
    args += includeField with config.include.size
    args += excludeField with config.exclude.size
    args += bootstrapField with Strings.isNotEmpty(config.bootstrap)
    args += logFailureConditions(config.failureConditions)
    when (config.script.name) {
      DEFAULT_SCRIPT_NAME -> {}
      "local-changes", "migrate-classes", "scoped" -> {
        args += scriptField with config.script.name
      }
      "php-migration" -> {
        args += scriptField with "php-migration"
        args += phpMigrationFromLevelField with config.script.parameters["fromLevel"]?.toString()
        args += phpMigrationToLevelField with config.script.parameters["toLevel"]?.toString()
      }
      else -> {
        args += scriptField with "other"
      }
    }
    args += onlyDirectoryField with (config.onlyDirectory != null)
    args += fixesStrategyField with config.fixesStrategy
    args += includeAbsentField with config.includeAbsent
    args += baselineField with when (config.baseline) {
      null -> "none"
      // TODO: use reserved 'cloud' value
      else -> "local"
    }
    configEvent.log(args)
  }

  @JvmStatic
  fun logLicense(license: QodanaLicense) {
    val args = mutableListOf<EventPair<*>>()
    args += licenseTypeField with license.type
    args += trialField with license.trial
    if (license.expirationDate != null) {
      val daysLeft = Duration.between(Instant.now(), license.expirationDate.toInstant()).toDays() + 1
      args += daysLeftField with daysLeft.toInt()
    }
    licenseEvent.log(args)
  }

  private fun logFailureConditions(failureConditions: FailureConditions) = buildList {
    infix fun EventField<Int>.logIfPresent(value: Int?) {
      if (value != null) add(this with value)
    }

    failureConditionAnySeverityField logIfPresent failureConditions.severityThresholds.any
    failureConditionCriticalField logIfPresent failureConditions.severityThresholds.critical
    failureConditionHighField logIfPresent failureConditions.severityThresholds.high
    failureConditionModerateField logIfPresent failureConditions.severityThresholds.moderate
    failureConditionLowField logIfPresent failureConditions.severityThresholds.low
    failureConditionInfoField logIfPresent failureConditions.severityThresholds.info
    failureConditionMinimumTotalCoverageField logIfPresent failureConditions.testCoverageThresholds.total
    failureConditionMinimumFreshCoverageField logIfPresent failureConditions.testCoverageThresholds.fresh
  }

  suspend fun logPromoGithubConfigPresent(project: Project) {
    GitHubCIFileChecker(project).ciFileFlow.filter { it !is CIFile.NotInitialized }.firstOrNull().let { ciFile ->
      if (ciFile !is CIFile.ExistingWithQodana) return@let
      try {
        val ciFileText = withContext(QodanaDispatchers.IO) { ciFile.virtualFile?.readText() }
        if (ciFileText?.contains(DefaultQodanaGithubWorkflowBuilder.PROMO_HEADER_TEXT) == true) {
          qodanaGithubPromoWorkflowUsed.log()
        }
      }
      catch (e: IOException) {
        Logger.getInstance(UsageCollector::class.java).warn("Couldn't read the contents of CI file", e)
      }
    }
  }

}
