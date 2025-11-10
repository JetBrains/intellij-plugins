// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.config

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.ComponentManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.util.removeUserData
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.scope.packageSet.*
import com.intellij.util.application
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.extensions.VcsIgnoredFilesProvider
import org.jetbrains.qodana.license.QodanaLicense
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.sarif.hasFixes
import org.jetbrains.qodana.staticAnalysis.script.DEFAULT_SCRIPT_NAME
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

const val QODANA_YAML_CONFIG_FILENAME = "qodana.yaml"
const val QODANA_YML_CONFIG_FILENAME = "qodana.yml"

const val EMBEDDED_GIT_IGNORE_EXCLUDE = "embedded.gitignore"

const val COVERAGE_OUTPUT_DIR = "coverage"

internal val DEFAULT_EXCLUDE_SCOPE_MODIFIER = GlobalExcludeScopeModifier(
  InspectScope(
    name = "embedded.default",
    paths = listOf("buildSrc", "vendor", "build", "builds", "dist", "tests", "tools", "vendor", "bin", ".qodana"),
    patterns = listOf("test:*..*", "file:buildSrc/*", "file[*.buildSrc]:*/"),
  )
)

internal val GIT_IGNORE_SCOPE_MODIFIER = GitIgnoreExcludeScopeModifier(
  InspectScope(EMBEDDED_GIT_IGNORE_EXCLUDE)
)

val DEFAULT_FIXES_STRATEGY = FixesStrategy.NONE.name

const val DEFAULT_FILE_SUSPEND_THRESHOLD = "5"
const val DEFAULT_MODULE_SUSPEND_THRESHOLD = "25"
const val DEFAULT_PROJECT_SUSPEND_THRESHOLD = "125"

typealias InspectScopes = List<InspectScope>

data class QodanaScriptConfig(
  val name: String = DEFAULT_SCRIPT_NAME,
  val parameters: Map<String, Any> = emptyMap()
)

data class QodanaCoverageConfig(
  val reportProblems: Boolean = QodanaCoverageYamlConfig().reportProblems,
  val coveragePath: Path
)

private val LOG = logger<QodanaConfig>()

data class InspectScope(val name: String = "", var paths: List<String> = emptyList(), val patterns: List<String> = emptyList()) {
  fun getProfileScope(projectPath: Path): PackageSet? {
    val sets = mutableListOf<PackageSet>()

    val packageSetFactory = PackageSetFactory.getInstance()

    try {
      patterns.mapTo(sets, packageSetFactory::compile)
    }
    catch (_: ParsingException) {
      LOG.debug("Cannot parse package set patterns")
    }

    if (paths.isNotEmpty()) {
      val pathsSet = object : AbstractPackageSet(paths.joinToString()) {
        val absolutePaths = paths.map {
          Path.of(projectPath.toString(), it)
        }

        override fun contains(file: VirtualFile, project: Project, holder: NamedScopesHolder?): Boolean {
          val path = Path.of(file.path)
          return absolutePaths.any { path.startsWith(it.toCanonicalPath()) }
        }
      }

      sets.add(pathsSet)
    }

    return if (sets.isEmpty()) null else UnionPackageSet.create(*sets.toTypedArray())
  }
}

fun getResultsStorage(outPath: Path, outputFormat: OutputFormat): Path {
  return if (outputFormat != OutputFormat.INSPECT_SH_FORMAT) {
    outPath.resolve("temp")
  }
  else {
    outPath
  }
}

private fun getDefaultProfileName(): String = System.getProperty("qodana.default.profile", "qodana.starter")

enum class FixesStrategy(val stageName: String) {
  NONE("No fixes"),
  APPLY("Apply all fixes"),
  CLEANUP("Apply cleanup fixes");

  companion object {
    fun fromString(value: String): FixesStrategy {
      try {
        return FixesStrategy.valueOf(value.uppercase(Locale.getDefault()))
      }
      catch (e: IllegalArgumentException) {
        throw QodanaException("Unknown value '$value' for fixesStrategy configuration parameter")
      }
    }
  }
}

private val QODANA_CONFIG_KEY = Key.create<QodanaConfig>("QODANA_CONFIG_KEY")

fun ComponentManager.addQodanaAnalysisConfig(config: QodanaConfig) {
  putUserData(QODANA_CONFIG_KEY, config)
}

fun ComponentManager.removeQodanaAnalysisConfig() {
  removeUserData(QODANA_CONFIG_KEY)
}

val Project.qodanaAnalysisConfig: QodanaConfig?
  get() = getUserData(QODANA_CONFIG_KEY)

/**
 * Not available in IDE run, use it for project configuration
 * If you need qodana config during analysis, use [Project.qodanaAnalysisConfig]
 */
// TODO – use in dotnet
val Application.qodanaAnalysisConfigForConfiguration: QodanaConfig?
  get() = getUserData(QODANA_CONFIG_KEY)

data class QodanaConfig(
  val projectPath: Path,
  val yamlFiles: QodanaYamlFiles,
  val outPath: Path,
  val resultsStorage: Path,
  val baseline: String?,
  val profile: QodanaProfileConfig,
  val profileSource: String,
  val defaultProfileName: String,
  val disableSanityInspections: Boolean,
  val fixesStrategy: FixesStrategy,
  val runPromoInspections: Boolean?,
  val script: QodanaScriptConfig,
  val includeAbsent: Boolean,
  val outputFormat: OutputFormat,
  var license: QodanaLicense,
  val onlyDirectory: Path?,
  val exclude: InspectScopes,
  val include: InspectScopes,
  val bootstrap: String?,
  val maxRuntimeNotifications: Int,
  val failOnErrorNotification: Boolean,
  val failureConditions: FailureConditions,
  val coverage: QodanaCoverageConfig,
  val hardcodedPasswords: HardcodedPasswords,
  val rootJavaProjects: List<Path>,

  val dotnet: DotNetProjectConfiguration?,
  val cpp: QodanaCppConfig?,
  val php: QodanaPhpConfig?,
  val jvm: QodanaJvmConfig,
  val dependencyAnalysis: DependencyAnalysisConfig,

  val skipPreamble: Boolean = System.getProperty("qodana.skip.preamble").toBoolean(), // Set by CLI for second run in scoped script
  val skipResultStrategy: SkipResultStrategy = SkipResultStrategy.fromParameters(),
  val stopThreshold: Int? = System.getProperty("qodana.stop.threshold")?.toInt(),
  val fileSuspendThreshold: Int = System.getProperty("qodana.file.suspend.threshold", DEFAULT_FILE_SUSPEND_THRESHOLD).toInt(),
  val moduleSuspendThreshold: Int = System.getProperty("qodana.module.suspend.threshold", DEFAULT_MODULE_SUSPEND_THRESHOLD).toInt(),
  val projectSuspendThreshold: Int = System.getProperty("qodana.project.suspend.threshold", DEFAULT_PROJECT_SUSPEND_THRESHOLD).toInt(),
) {

  companion object {
    fun fromYaml(
      projectPath: Path,
      outPath: Path,
      yamlFiles: QodanaYamlFiles = QodanaYamlFiles.noFiles(),
      yaml: QodanaYamlConfig = QodanaYamlConfig.EMPTY_V1,
      resultsStorage: Path = getResultsStorage(outPath, getOutputFormat()),
      baseline: String? = null,
      profileNameFromCli: String = "",
      profilePathFromCli: String = "",
      profile: QodanaProfileConfig = QodanaProfileConfig.fromYaml(yaml.profile, profileNameFromCli, profilePathFromCli),
      defaultProfileName: String = getDefaultProfileName(),
      disableSanityInspections: Boolean = yaml.disableSanityInspections,
      fixesStrategy: FixesStrategy = FixesStrategy.fromString(yaml.fixesStrategy),
      runPromoInspections: Boolean? = yaml.runPromoInspections,
      script: QodanaScriptConfig = yaml.script,
      includeAbsent: Boolean = yaml.includeAbsent,
      outputFormat: OutputFormat = getOutputFormat(),
      license: QodanaLicense = QodanaLicense(QodanaLicenseType.ULTIMATE_PLUS, false, null),
      onlyDirectory: Path? = yaml.onlyDirectory,
      exclude: InspectScopes = yaml.exclude,
      include: InspectScopes = yaml.include,
      bootstrap: String? = yaml.bootstrap,
      maxRuntimeNotifications: Int = yaml.maxRuntimeNotifications,
      failOnErrorNotification: Boolean = yaml.failOnErrorNotification,
      failureConditions: FailureConditions = yaml.failureConditions,
      coverage: QodanaCoverageConfig = QodanaCoverageConfig(
        reportProblems = yaml.coverage.reportProblems,
        coveragePath = outPath.resolve("$COVERAGE_OUTPUT_DIR/")
      ),
      rootJavaProjects: List<Path> = yaml.rootJavaProjects
    ): QodanaConfig {
      val dotnet = yaml.dotnet
      val php = yaml.php
      val jvm = QodanaJvmConfig(yaml.projectJDK)
      val dependencyAnalysis = DependencyAnalysisConfig.fromYamlConfig(yaml)

      if (yaml.version != "1.0") throw QodanaException("Property \"version\" in qodana.yaml must be \"1.0\", not \"${yaml.version}\"")
      val isCommandLineProfile = profileNameFromCli.isNotBlank() || profilePathFromCli.isNotBlank()
      return QodanaConfig(
        projectPath = projectPath,
        outPath = outPath,
        yamlFiles = yamlFiles,
        resultsStorage = resultsStorage,
        baseline = baseline,
        profile = profile,
        profileSource = if (isCommandLineProfile) "command line" else QODANA_YAML_CONFIG_FILENAME,
        defaultProfileName = defaultProfileName,
        disableSanityInspections = disableSanityInspections,
        fixesStrategy = fixesStrategy,
        runPromoInspections = runPromoInspections,
        script = script,
        includeAbsent = includeAbsent,
        outputFormat = outputFormat,
        license = license,
        onlyDirectory = onlyDirectory,
        exclude = exclude,
        include = include,
        bootstrap = bootstrap,
        maxRuntimeNotifications = maxRuntimeNotifications,
        failOnErrorNotification = failOnErrorNotification,
        failureConditions = failureConditions,
        coverage = coverage,
        hardcodedPasswords = HardcodedPasswords.fromConfig(yaml.hardcodedPasswords),
        dotnet = dotnet,
        cpp = yaml.cpp,
        php = php,
        jvm = jvm,
        dependencyAnalysis = dependencyAnalysis,
        rootJavaProjects = rootJavaProjects
      )
    }
  }

  init {
    // Allow non-absolute paths for windows tests, where temp://src is treatet as not absolute
    if (!application.isUnitTestMode && !projectPath.isAbsolute) throw QodanaException("Project path \"$projectPath\" must be absolute")
    if (failOnErrorNotification && maxRuntimeNotifications < 1) {
      throw QodanaException("Cannot enable 'failOnErrorNotification' when 'maxRuntimeNotifications' is less than 1")
    }
  }

  fun checkRunPromo(inspectionProfile: QodanaInspectionProfile): Boolean {
    return runPromoInspections == true ||
           (runPromoInspections == null && inspectionProfile.name == defaultProfileName)
  }

  fun getIncludeModifiers(): List<QodanaScopeModifier> =
    include.map(::DefaultSeverityIncludeScopeModifier)

  private fun supportsGitIgnore(project: Project): Boolean {
    val ignored = VcsIgnoredFilesProvider.getVcsRepositoriesIgnoredFiles(project)
    return ignored.isNotEmpty()
  }

  fun getExcludeModifiers(addDefaultExclude: Boolean, project: Project): List<QodanaScopeModifier> {
    val modifiers = mutableListOf<QodanaScopeModifier>()
    for (scope in exclude) {
      if (scope.name == "All")
        modifiers += GlobalExcludeScopeModifier(scope)
      else
        modifiers += ExcludeScopeModifier(scope)
    }
    if (addDefaultExclude) modifiers += DEFAULT_EXCLUDE_SCOPE_MODIFIER
    if (addDefaultExclude && supportsGitIgnore(project)) modifiers += GIT_IGNORE_SCOPE_MODIFIER
    return modifiers
  }

  fun isAboveStopThreshold(count: Int): Boolean = stopThreshold != null && count > stopThreshold
}

enum class SkipResultStrategy {
  ALWAYS, // always skip posting results; set by CLI for first run in the scoped script
  ANY, // skip posting results on any non-empty result
  FIXABLE,  // skip posting results on any fixable result
  NEVER; // never skip posting results

  companion object {
    fun fromParameters(): SkipResultStrategy {
      System.getProperty("qodana.skip.result.strategy")?.let {
        return SkipResultStrategy.valueOf(it)
      }
      if (System.getProperty("qodana.skip.result").toBoolean()) return ALWAYS
      return NEVER
    }
  }

  suspend fun shouldSkip(run: Run): Boolean {
    val newByBaseline: (Result) -> Boolean = { result ->
      result.baselineState == null
      || (result.baselineState != Result.BaselineState.ABSENT
          && result.baselineState != Result.BaselineState.UNCHANGED)
    }
    return withContext(StaticAnalysisDispatchers.Default) {
      when (this@SkipResultStrategy) {
        ALWAYS -> true
        ANY -> run.results.any { newByBaseline(it) }
        FIXABLE -> run.results.any { newByBaseline(it) && it.hasFixes() }
        NEVER -> false
      }
    }
  }
}

/**
 * Always write qodana.yaml even if config was read from file with different name
 */
internal suspend fun copyConfigToDir(config: QodanaConfig, outDir: Path = PathManager.getLogDir()) {
  runInterruptible(StaticAnalysisDispatchers.IO) {
    val filesWithNamesInDir = listOfNotNull(
      config.yamlFiles.localQodanaYaml?.let { it to QodanaYamlFiles.LOCAL_QODANA_YAML_FILENAME },
      config.yamlFiles.qodanaConfigJson?.let { it to QodanaYamlFiles.QODANA_CONFIG_JSON_FILENAME },

      // QD-10850
      config.yamlFiles.effectiveQodanaYaml?.let { it to QodanaYamlFiles.LOCAL_QODANA_YAML_FILENAME },
    )

    filesWithNamesInDir.forEach { (file, nameInLog) ->
      val pathInDir = outDir.resolve(nameInLog)
      runCatching { Files.deleteIfExists(pathInDir) }

      Files.copy(file, pathInDir, StandardCopyOption.REPLACE_EXISTING)
    }
  }
}
