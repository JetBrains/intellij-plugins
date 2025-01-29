package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.qodana.staticAnalysis.inspections.config.DependencyAnalysisConfig.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
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

  /**
   * profile path is constructed either from [yamlPath] or from [projectPath], [projectPath] is in priority
   *
   * - Q: why projectPath is used?
   *   A: because it was implemented like this, and was present in this form (without [yamlPath]) in 2024.3, since 205.1 [yamlPath] is used aswell
   */
  fun withAbsoluteProfilePath(
    projectPath: Path,
    yamlPath: Path,
  ): QodanaYamlConfig {
    val profilePath = profile.path
    if (profilePath.isBlank() || Paths.get(profilePath).isAbsolute) {
      return this
    }

    val absoluteFromProjectPath = projectPath.resolve(profilePath).toAbsolutePath()
    val absoluteFromYamlPath = yamlPath.parent.resolve(profilePath).toAbsolutePath()
    val absolutePath = absoluteFromProjectPath.takeIf { it.exists() } ?: absoluteFromYamlPath

    return copy(profile = profile.copy(path = absolutePath.pathString))
  }
}
