package org.jetbrains.qodana.staticAnalysis.inspections.config


/**
 * Class for storing and working with the configuration file: qodana.yaml.
 *
 * @param [licenseRules] list of allow or prohibited licenses. Look [LicenseRule]
 * @param [dependencyOverrides] list of overridden dependencies. Look [DependencyOverride]
 * @param [dependencyIgnores] list of ignore dependencies. Look [DependencyIgnore]
 * @param [customDependencies] list of custom dependencies. Look [CustomDependency]
 */
data class DependencyAnalysisConfig(
  val projectLicenses: List<LicenseOverride> = emptyList(),
  val licenseRules: List<LicenseRule> = emptyList(),
  val dependencyOverrides: List<DependencyOverride> = emptyList(),
  val dependencyIgnores: List<DependencyIgnore> = emptyList(),
  val customDependencies: List<CustomDependency> = emptyList(),
  val modulesToAnalyze: Set<AllowedModule> = emptySet(),
  val dependencySbomExclude: Set<DependencyIgnore> = emptySet(), // not in SBOM, but in projectMetadata
  val analyzeDevDependencies: Boolean = false,
  val enablePackageSearch: Boolean = true,
  val raiseLicenseProblems: Boolean = false
) {

  companion object {
    fun fromYamlConfig(yaml: QodanaYamlConfig): DependencyAnalysisConfig {
      return DependencyAnalysisConfig(
        projectLicenses = yaml.projectLicenses,
        licenseRules = yaml.licenseRules,
        dependencyOverrides = yaml.dependencyOverrides,
        dependencyIgnores = yaml.dependencyIgnores,
        customDependencies = yaml.customDependencies,
        modulesToAnalyze = yaml.modulesToAnalyze,
        dependencySbomExclude = yaml.dependencySbomExclude,
        analyzeDevDependencies = yaml.analyzeDevDependencies,
        enablePackageSearch = yaml.enablePackageSearch,
        raiseLicenseProblems = yaml.raiseLicenseProblems
      )
    }
  }

  data class LicenseOverride(
    val key: String = "UNKNOWN",
    val url: String? = null,
  )

  data class DependencyOverride(val name: String = "",
                                val version: String = "",
                                val url: String? = null,
                                val copyrightText: String? = null,
                                val licenses: List<LicenseOverride> = listOf())

  data class CustomDependency(val name: String = "",
                              val version: String = "",
                              val module: String? = null,
                              val url: String? = null,
                              val copyrightText: String? = null,
                              val licenses: List<LicenseOverride> = listOf())

  data class LicenseRule(val keys: List<String> = listOf(), val prohibited: List<String> = listOf(), val allowed: List<String> = listOf())

  data class DependencyIgnore(val name: String = "")

  data class AllowedModule(val name: String = "")

}
