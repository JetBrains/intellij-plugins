package com.intellij.openRewrite.gradle

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openRewrite.run.OpenRewriteState
import com.intellij.openRewrite.run.before.OpenRewriteInstallBeforeRunTask
import com.intellij.openRewrite.run.executeInLocalHistoryAction
import com.intellij.openRewrite.run.splitConfigurationValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfigurationViewManager
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration
import org.jetbrains.plugins.gradle.service.task.GradleTaskManager
import java.util.function.Supplier

internal class OpenRewriteGradleRunConfiguration(private val base: OpenRewriteRunConfiguration) :
  GradleRunConfiguration(base.project,
                         GradleExternalTaskConfigurationType.getInstance().configurationFactories[0],
                         base.name) {
  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
    OpenRewriteGradleState(this, environment).apply { this@OpenRewriteGradleRunConfiguration.copyUserDataTo(this) }

  internal class OpenRewriteGradleState(
    private val configuration: OpenRewriteGradleRunConfiguration,
    executionEnvironment: ExecutionEnvironment,
  ) :
    ExternalSystemRunnableState(configuration.settings, configuration.project, false, configuration, executionEnvironment),
    OpenRewriteState {

    private var recipeArtifacts: Collection<String> = emptyList()

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
      putUserData(GradleTaskManager.INIT_SCRIPT_KEY, getInitScript(configuration.base, recipeArtifacts))
      putUserData(GradleTaskManager.INIT_SCRIPT_PREFIX_KEY, "rewrite")

      putUserData(PROGRESS_LISTENER_KEY, ExternalSystemRunConfigurationViewManager::class.java)
      val supplier = Supplier {
        var result: ExecutionResult? = null
        ApplicationManager.getApplication().invokeAndWait { result = super.execute(executor, runner) }
        return@Supplier result
      }

      if (configuration.base.dryRun) {
        return supplier.get()
      }
      return executeInLocalHistoryAction(configuration.name, configuration.project, configuration.settings.externalProjectPath, supplier)
    }

    override fun setRecipeArtifacts(artifacts: Collection<String>) {
      recipeArtifacts = artifacts
    }
  }
}

private fun getInitScript(configuration: OpenRewriteRunConfiguration, recipeArtifacts: Collection<String>): String {
  val artifacts = recipeArtifacts.map { "\"$it\"" }
  val dependencies = if (artifacts.isEmpty()) "" else toDependenciesBlock(artifacts)
  val recipes = toQuotedList(configuration.activeRecipes)
  val activeRecipeOption = if (recipes.isEmpty()) "" else "activeRecipe($recipes)"
  val styles = toQuotedList(configuration.activeStyles)
  val activeStyleOption = if (styles.isEmpty()) "" else "activeStyle($styles)"

  val exclusions = toQuotedList(configuration.exclusions)
  val exclusionsOption = if (exclusions.isEmpty()) "" else "exclusion($exclusions)"

  val plainTextMasks = toQuotedList(configuration.plainTextMasks)
  val plainTextMasksOption = if (plainTextMasks.isEmpty()) "" else "plainTextMask($plainTextMasks)"

  val configLocation = configuration.getExpandedConfigLocation()
  val configFileOption = if (configLocation.isNullOrBlank()) "" else "configFile = new File(\"$configLocation\")"

  val version = configuration.libraryVersion ?: "latest.release"

  val requireLocal = configuration.beforeRunTasks.filterIsInstance<OpenRewriteInstallBeforeRunTask>().isNotEmpty()
  val localRepositories = if (requireLocal) "repositories {\n    mavenCentral()\n}\n" else ""

  return """
      initscript {
          repositories {
              maven { url "https://plugins.gradle.org/m2" }
          }
          dependencies {
              classpath("org.openrewrite:plugin:$version")
          }
      }

      rootProject {
          afterEvaluate {
              if (!getPluginManager().hasPlugin("org.openrewrite.rewrite")) {
                  plugins.apply(org.openrewrite.gradle.RewritePlugin)   
              }
              $dependencies
              rewrite {
                  $activeRecipeOption
                  $activeStyleOption
                  $exclusionsOption
                  $plainTextMasksOption
                  $configFileOption
              }
              if (repositories.isEmpty()) {
                  repositories {
                      mavenCentral()
                  }
              }
              $localRepositories
          }
      }
    """.trimIndent()
}

private fun toQuotedList(value: String?): String {
  return splitConfigurationValue(value).joinToString(", ") { "\"$it\"" }
}

private fun toDependenciesBlock(dependencies: List<String>): String {
  val sb = StringBuilder("dependencies {\n")
  for (dependency in dependencies) {
    sb.append("    rewrite(").append(dependency).append(")\n")
  }
  sb.append("}\n")
  return sb.toString()
}