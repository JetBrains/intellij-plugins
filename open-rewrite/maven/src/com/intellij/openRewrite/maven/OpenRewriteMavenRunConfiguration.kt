package com.intellij.openRewrite.maven

import com.intellij.build.DefaultBuildDescriptor
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openRewrite.run.OpenRewriteState
import com.intellij.openRewrite.run.executeInLocalHistoryAction
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import org.jetbrains.idea.maven.execution.MavenRunConfiguration
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.run.MavenCommandLineState
import java.util.concurrent.ExecutionException
import java.util.function.Function

internal class OpenRewriteMavenRunConfiguration(private val base: OpenRewriteRunConfiguration) :
  MavenRunConfiguration(base.project,
                        MavenRunConfigurationType.getInstance().configurationFactories[0],
                        base.name) {
  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState = OpenRewriteMavenState(environment, this)

  private class OpenRewriteMavenState(environment: ExecutionEnvironment,
                                      private val configuration: OpenRewriteMavenRunConfiguration
  ) :
    MavenCommandLineState(environment, configuration), OpenRewriteState {

    private var recipeArtifacts: Collection<String> = emptyList()

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
      configuration.runnerParameters.commandLine = getCommandLine(configuration.base, recipeArtifacts)
      if (configuration.base.dryRun) {
        return super.execute(executor, runner)
      }
      return executeInLocalHistoryAction(configuration.name,
                                         configuration.project,
                                         configuration.runnerParameters.workingDirPath) { super.execute(executor, runner) }!!
    }

    override fun doRunExecute(executor: Executor,
                              runner: ProgramRunner<*>,
                              taskId: ExternalSystemTaskId?,
                              descriptor: DefaultBuildDescriptor?,
                              processHandler: ProcessHandler,
                              targetFileMapper: Function<String, String>): ExecutionResult {
      val console = createConsole(executor, processHandler, configuration.project)
      console?.attachToProcess(processHandler)
      return DefaultExecutionResult(console, processHandler, *createActions(console, processHandler, executor))
    }

    override fun setRecipeArtifacts(artifacts: Collection<String>) {
      recipeArtifacts = artifacts
    }
  }
}

private fun getCommandLine(configuration: OpenRewriteRunConfiguration, recipeArtifacts: Collection<String>): String {
  val commandLineBuilder = StringBuilder("-U org.openrewrite.maven:rewrite-maven-plugin")
  val version = configuration.libraryVersion
  if (version != null) {
    commandLineBuilder.append(":$version")
  }
  commandLineBuilder.append(":${getTaskName(configuration)}")
  if (recipeArtifacts.isNotEmpty()) {
    commandLineBuilder.append(" -Drewrite.recipeArtifactCoordinates=\"${recipeArtifacts.joinToString()}\"")
  }
  val activeRecipes = configuration.activeRecipes
  if (!activeRecipes.isNullOrBlank()) {
    commandLineBuilder.append(" -Drewrite.activeRecipes=\"${activeRecipes}\"")
  }
  val activeStyles = configuration.activeStyles
  if (!activeStyles.isNullOrBlank()) {
    commandLineBuilder.append(" -Drewrite.activeStyles=\"${activeStyles}\"")
  }
  val exclusions = configuration.exclusions
  if (!exclusions.isNullOrBlank()) {
    commandLineBuilder.append(" -Drewrite.exclusions=\"${exclusions}\"")
  }
  val plainTextMasks = configuration.plainTextMasks
  if (!plainTextMasks.isNullOrBlank()) {
    commandLineBuilder.append(" -Drewrite.plainTextMasks=\"${plainTextMasks}\"")
  }
  val configLocation = configuration.getExpandedConfigLocation()
  if (!configLocation.isNullOrBlank()) {
    commandLineBuilder.append(" -Drewrite.configLocation=\"${configLocation}\"")
  }
  return commandLineBuilder.toString()
}

private fun getTaskName(configuration: OpenRewriteRunConfiguration): String = if (configuration.dryRun) "dryRun" else "run"