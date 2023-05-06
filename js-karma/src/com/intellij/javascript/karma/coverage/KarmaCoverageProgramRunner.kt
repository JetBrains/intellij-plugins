package com.intellij.javascript.karma.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageExecutor
import com.intellij.coverage.CoverageHelper
import com.intellij.coverage.CoverageRunnerData
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.javascript.karma.KarmaBundle
import com.intellij.javascript.karma.execution.KarmaConsoleView
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunProgramRunner
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.util.KarmaUtil
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.concurrency.Promise
import java.io.File
import java.io.IOException
import java.nio.file.Path

class KarmaCoverageProgramRunner : AsyncProgramRunner<RunnerSettings>() {
  override fun getRunnerId(): String = KarmaCoverageProgramRunner::class.java.simpleName

  override fun canRun(executorId: String, profile: RunProfile): Boolean =
    CoverageExecutor.EXECUTOR_ID == executorId && profile is KarmaRunConfiguration

  override fun createConfigurationData(settingsProvider: ConfigurationInfoProvider): RunnerSettings = CoverageRunnerData()

  override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
    return KarmaRunProgramRunner.executeAsync(environment, state).then { executionResult ->
      val descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment)
      val consoleView = KarmaConsoleView.get(executionResult, state) ?: return@then descriptor
      val server = consoleView.karmaServer
      if (executionResult.processHandler is NopProcessHandler) {
        server.onBrowsersReady { ExecutionUtil.restartIfActive(descriptor) }
      }
      else {
        Handler.listenForCoverageFile(environment, server, NodeTargetRun.getTargetRun(executionResult.processHandler))
      }
      return@then descriptor
    }
  }

  private object Handler { // not a companion object to load less bytecode simultaneously with KarmaCoverageProgramRunner
    fun listenForCoverageFile(env: ExecutionEnvironment, server: KarmaServer, targetRun: NodeTargetRun) {
      val runConfiguration = env.runProfile as RunConfigurationBase<*>
      val coverageEnabledConfiguration = CoverageEnabledConfiguration.getOrCreate(runConfiguration)
      CoverageHelper.resetCoverageSuit(runConfiguration)
      val coverageFilePath = coverageEnabledConfiguration.coverageFilePath
      if (coverageFilePath != null) {
        server.coveragePeer!!.startCoverageSession { coverageResultPaths ->
          logger<KarmaCoverageProgramRunner>().info("Processing karma coverage file: ${coverageResultPaths?.localLcovFilePath}")
          ReadAction.run<RuntimeException> {
            val project = env.project
            if (!project.isDisposed) {
              if (coverageResultPaths != null) {
                processLcovInfoFile(coverageResultPaths, coverageFilePath, env, server, runConfiguration, targetRun)
                return@run
              }
              ApplicationManager.getApplication().invokeLater(
                {
                  val response = Messages.showYesNoDialog(project,
                                                          KarmaBundle.message(
                                                            "coverage.cannot_find_lcov.dialog.message"),
                                                          KarmaBundle.message(
                                                            "coverage.cannot_find_lcov.dialog.title"),
                                                          KarmaBundle.message(
                                                            "coverage.cannot_find_lcov.select_lcov.button",
                                                            "lcov.info"),
                                                          KarmaBundle.message(
                                                            "coverage.cannot_find_lcov.cancel.button"),
                                                          Messages.getWarningIcon())
                  if (response == Messages.YES) {
                    FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null, null) {
                      it?.toNioPath()?.let {
                        ApplicationManager.getApplication().executeOnPooledThread {
                          ReadAction.run<RuntimeException> {
                            if (!project.isDisposed) {
                              val paths = KarmaCoverageResultPaths(it, Path.of(
                                (runConfiguration as KarmaRunConfiguration).runSettings.workingDirectorySystemDependent))
                              processLcovInfoFile(paths, coverageFilePath, env, server,
                                                  runConfiguration, targetRun)
                            }
                          }
                        }
                      }
                    }
                  }
                },
                ModalityState.defaultModalityState()
              )
            }
          }
        }
      }
    }

    private fun processLcovInfoFile(coverageResultPaths: KarmaCoverageResultPaths,
                                    toCoverageFilePath: String,
                                    env: ExecutionEnvironment,
                                    karmaServer: KarmaServer,
                                    runConfiguration: RunConfigurationBase<*>,
                                    targetRun: NodeTargetRun) {
      try {
        FileUtil.copy(coverageResultPaths.localLcovFilePath.toFile(), File(toCoverageFilePath))
      }
      catch (e: IOException) {
        logger<KarmaCoverageProgramRunner>().error("Cannot copy " + coverageResultPaths.localLcovFilePath + " to " + toCoverageFilePath, e)
        return
      }
      env.runnerSettings?.let {
        val coverageRunner = KarmaCoverageRunner.getInstance()
        coverageRunner.setTargetRun(targetRun)
        coverageRunner.setProjectRoot(coverageResultPaths.localProjectRoot)
        CoverageDataManager.getInstance(env.project).processGatheredCoverage(runConfiguration, it)
      }
    }
  }
}