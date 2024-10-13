package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.qodana.staticAnalysis.inspections.config.DependencyAnalysisConfig.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

data class QodanaCoverageYamlConfig(
  val reportProblems: Boolean = true
)

data class QodanaYamlConfig(
  val version: String = "1.0",
  val profile: QodanaProfileConfig = QodanaProfileConfig(),
  val exclude: InspectScopes = emptyList(),
  val include: InspectScopes = emptyList(),
  val disableSanityInspections: Boolean = false,
  val fixesStrategy: String = DEFAULT_FIXES_STRATEGY,
  val runPromoInspections: Boolean? = null,
  val bootstrap: String? = null,
  val script: QodanaScriptConfig = QodanaScriptConfig(),
  val includeAbsent: Boolean = false,
  val failOnErrorNotification: Boolean = false,
  val maxRuntimeNotifications: Int = 100,
  val coverage: QodanaCoverageYamlConfig = QodanaCoverageYamlConfig(),
  // deprecated, but used as anySeverity
  private val failThreshold: Int? = null,
  val failureConditions: FailureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = failThreshold)),
  val hardcodedPasswords: HardcodedPasswordsConfig = HardcodedPasswordsConfig(),

  // dotnet
  val dotnet: DotNetProjectConfiguration? = null,

  // php
  val php: QodanaPhpConfig? = null,

  // jvm
  val projectJDK: String? = null,

  // license audit
  val projectLicenses: List<LicenseOverride> = emptyList(),
  val licenseRules: List<LicenseRule> = emptyList(),
  val dependencyOverrides: List<DependencyOverride> = emptyList(),
  val dependencyIgnores: List<DependencyIgnore> = emptyList(),
  val customDependencies: List<CustomDependency> = emptyList(),
  val modulesToAnalyze: Set<AllowedModule> = emptySet(),
  val dependencySbomExclude: Set<DependencyIgnore> = emptySet(), // not in SBOM, but in projectMetadata
  val analyzeDevDependencies: Boolean = false,
  val enablePackageSearch: Boolean = false,
  val raiseLicenseProblems: Boolean = false
) {

  companion object {
    val EMPTY_V1 = QodanaYamlConfig(version = "1.0")
  }

  fun withAbsoluteProfilePath(projectPath: Path): QodanaYamlConfig {
    val profilePath = profile.path
    return if (profilePath.isBlank() || Paths.get(profilePath).isAbsolute) {
      this
    }
    else {
      val absolute = projectPath.resolve(profilePath).toAbsolutePath().pathString
      copy(profile = profile.copy(path = absolute))
    }
  }
}
