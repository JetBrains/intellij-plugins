// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.cli

import com.intellij.execution.RunManager
import com.intellij.ide.file.BatchFileChangeListener
import com.intellij.javascript.CreateRunConfigurationUtil
import com.intellij.javascript.nodejs.packageJson.PackageJsonDependenciesExternalUpdateManager
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.buildTools.npm.NpmScriptsUtil
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfigurationBuilder
import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigManager
import com.intellij.lang.javascript.buildTools.webpack.WebPackConfiguration
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Pair.pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.io.exists
import org.jetbrains.vuejs.VueBundle
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JPanel

class VueCliRunningGeneratorController internal constructor(generationLocation: Path,
                                                            private val settings: NpmPackageProjectGenerator.Settings,
                                                            private val listener: VueRunningGeneratorListener,
                                                            parentDisposable: Disposable) : Disposable {
  // checked in disposed condition
  @Volatile
  private var state: VueProjectCreationState = VueProjectCreationState.Starting
  private var currentQuestion: VueCreateProjectProcess.Question? = null
  private var process: VueCreateProjectProcess? = null
  private var questionUi: VueCliGeneratorQuestioningPanel? = null

  init {
    Disposer.register(parentDisposable, this)
    state = VueProjectCreationState.Process
    val templateName = settings.getUserData(VueCliGeneratorSettingsPeer.TEMPLATE_KEY)!!
    val projectName = generationLocation.fileName.toString()

    val isOldPackage = Paths.get(settings.myPackage.systemDependentPath).fileName.toString() == "vue-cli"
    questionUi = VueCliGeneratorQuestioningPanel(isOldPackage, templateName, projectName,
                                                 {
                                                   if (state == VueProjectCreationState.User) {
                                                     if (it) listener.enableNext()
                                                     else listener.disableNext(null)
                                                   }
                                                 })
    process = VueCreateProjectProcess(generationLocation.parent, projectName, templateName, settings.myInterpreterRef,
                                      settings.myPackage.systemDependentPath, this)
    process!!.listener = {
      ApplicationManager.getApplication().invokeLater(
        Runnable {
          if (state != VueProjectCreationState.Process) return@Runnable

          val processState = process!!.getState()
          if (VueProjectCreationState.Error == processState.processState) {
            questionUi!!.error()
            listener.error(processState.globalProblem)
            state = VueProjectCreationState.Error
            process!!.listener = null
            process!!.cancel()
          }
          else if (VueProjectCreationState.QuestionsFinished == processState.processState ||
                   VueProjectCreationState.Finished == processState.processState) {
            state = VueProjectCreationState.QuestionsFinished
            val callback: (Project) -> Unit = { project ->
              val publisher = BackgroundTaskUtil.syncPublisher(BatchFileChangeListener.TOPIC)
              publisher.batchChangeStarted(project, VueBundle.message("vue.project.generator.progress.task.name"))
              val packageJson = PackageJsonUtil.findChildPackageJsonFile(project.baseDir)
              val doneCallback = PackageJsonDependenciesExternalUpdateManager.getInstance(project).externalUpdateStarted(packageJson, null)
              StartupManager.getInstance(project).runWhenProjectIsInitialized {
                createListeningProgress(project, Runnable {
                  publisher.batchChangeCompleted(project)
                  doneCallback.run()
                }, generationLocation)
              }
            }
            listener.finishedQuestionsCloseUI(callback)
          }
          else if (VueProjectCreationState.Process == processState.processState) {
            currentQuestion = processState.question
            val error = processState.question?.validationError
            if (error != null) {
              listener.disableNext(error)
              questionUi!!.activateUi()
            }
            else {
              listener.disableNext("")
              questionUi!!.question(processState.question!!)
            }
            state = VueProjectCreationState.User
            listener.enableNext()
          }
        }, ModalityState.any(),
        Condition<Boolean> { state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error })
    }
  }

  private fun getVueCliVersion(): Number {
    val vueCliPackage = settings.myPackage.systemIndependentPath
    return when {
      vueCliPackage.endsWith("/@vue/cli") -> 3
      vueCliPackage.endsWith("/vue-cli") -> 2
      else -> 0
    }
  }

  private fun createListeningProgress(project: Project,
                                      doneCallback: Runnable,
                                      generationLocation: Path) {
    if (process == null) return
    val task = object : Task.Backgroundable(project, VueBundle.message("vue.project.generator.progress.task.name.dots"), false,
                                            PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      override fun run(indicator: ProgressIndicator) {
        process!!.waitForProcessTermination(indicator)
      }

      override fun onFinished() {
        ReadAction.run<RuntimeException> {
          if (project.isDisposed) return@run
          doneCallback.run()
          JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
          setupWebpackConfigFile(project)
          CreateRunConfigurationUtil.debugConfiguration(project, 8080)
          createNpmRunConfiguration(project)
          LocalFileSystem.getInstance().refreshIoFiles(listOf(generationLocation.toFile()), true, true, null)
        }
      }
    }
    val indicator = BackgroundableProcessIndicator(task)
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
  }


  private fun setupWebpackConfigFile(project: Project) {
    val path = when (getVueCliVersion()) {
      3 -> project.basePath + "/node_modules/@vue/cli-service/webpack.config.js"
      2 -> project.basePath + "/build/webpack.dev.conf.js"
      else -> null
    }
    if (path != null && File(path).isFile) {
      WebPackConfigManager.instance(project).loadState(WebPackConfiguration(path))
    }
  }

  private fun createNpmRunConfiguration(project: Project) {
    val runManager = RunManager.getInstance(project)
    val pkg = PackageJsonUtil.findChildPackageJsonFile(project.baseDir)
    if (pkg != null) {
      val npmScripts = NpmScriptsUtil.listTasks(project, pkg).scripts
      if (npmScripts.find { it.name == "start" || it.name == "serve" || it.name == "dev" } == null && getVueCliVersion() != 3) {
        return
      }
      var scriptName = "serve"
      when {
        npmScripts.find { it.name == "start" } != null -> scriptName = "start"
        npmScripts.find { it.name == "start" || it.name == "serve" } == null && npmScripts.find { it.name == "dev" } != null -> scriptName = "dev"
      }
      val startConfiguration = NpmRunConfigurationBuilder(project).createRunConfiguration("npm $scriptName", null, pkg.path,
                                                                                          ContainerUtil.newHashMap<String, Any>(
                                                                                            pair<String, String>("run-script", scriptName)))
      runManager.selectedConfiguration = startConfiguration
    }
  }

  fun isFinished(): Boolean {
    return state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error
  }

  fun getPanel(): JPanel {
    return questionUi!!.panel
  }

  fun onNext() {
    @Suppress("CascadeIf")
    if (state == VueProjectCreationState.Process) {
      // we do not control Next button in the wizard so good by now - just skip it
      //assert(false, { "should be waiting for process" })
    }
    else if (state == VueProjectCreationState.User) {
      assert(currentQuestion != null)
      listener.disableNext(null)
      if (currentQuestion!!.type == VueCreateProjectProcess.QuestionType.Checkbox) {
        val answer = questionUi!!.getCheckboxAnswer()!!
        process!!.answer(answer)
      }
      else {
        val answer = questionUi!!.getAnswer()!!
        process!!.answer(answer)
      }
      state = VueProjectCreationState.Process
      questionUi!!.waitForNextQuestion()
    }
    else if (state == VueProjectCreationState.QuestionsFinished) {
      listener.cancelCloseUI()
    }
  }

  override fun dispose() {
    stopProcess()
  }

  fun stopProcess() {
    if (state != VueProjectCreationState.QuestionsFinished) process?.cancel()
  }
}

fun createVueRunningGeneratorController(generationLocation: String,
                                        settings: NpmPackageProjectGenerator.Settings,
                                        listener: VueRunningGeneratorListener,
                                        parentDisposable: Disposable): VueCliRunningGeneratorController? {
  val location = validateProjectLocation(generationLocation)
  return if (location.first != null) VueCliRunningGeneratorController(location.first!!, settings, listener, parentDisposable)
  else {
    listener.disableNext(location.second)
    null
  }
}

private fun validateProjectLocation(projectLocation: String): Pair<Path?, String?> {
  var location: Path? = null
  var error: String? = null
  try {
    location = Paths.get(projectLocation).normalize()
    val parentFolder = location.parent
    if (!parentFolder.exists() && !FileUtil.createDirectory(parentFolder.toFile())) {
      error = "Can not create project directory: %s"
    }
  }
  catch (e: InvalidPathException) {
    error = "Invalid project path: %s"
  }
  if (error != null) {
    return Pair(null, String.format(error, projectLocation))
  }
  return Pair(location, null)
}

interface VueRunningGeneratorListener {
  fun enableNext()
  fun disableNext(validationError: String?)
  fun error(validationError: String?)
  fun finishedQuestionsCloseUI(callback: (Project) -> Unit)
  fun cancelCloseUI()
}