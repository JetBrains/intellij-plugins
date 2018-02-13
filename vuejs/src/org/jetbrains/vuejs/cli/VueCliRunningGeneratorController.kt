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

import com.intellij.ide.RecentProjectsManager
import com.intellij.internal.statistic.UsageTrigger
import com.intellij.javascript.nodejs.packageJson.PackageJsonDependenciesExternalUpdateManager
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.util.PathUtil
import com.intellij.util.io.exists
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.JPanel

/**
 * @author Irina.Chernushina on 2/12/2018.
 */
class VueCliRunningGeneratorController internal constructor (private val projectLocation: Path,
                                                             settings: NpmPackageProjectGenerator.Settings,
                                                             private val listener: VueRunningGeneratorListener): Disposable {
  // checked in disposed condition
  @Volatile private var state: VueProjectCreationState = VueProjectCreationState.Init
  private var currentQuestion: VueCreateProjectProcess.Question? = null
  private var process: VueCreateProjectProcess? = null
  private var questionUi: VueCliGeneratorQuestioningPanel? = null

  init {
    state = VueProjectCreationState.Process
    val templateName = settings.getUserData(VueCliGeneratorSettingsPeer.TEMPLATE_KEY)!!
    val projectName = projectLocation.fileName.toString()

    val isOldPackage = Paths.get(settings.myPackagePath).fileName.toString() == "vue-cli"
    questionUi = VueCliGeneratorQuestioningPanel(isOldPackage, templateName, projectName,
                                                 { if (state == VueProjectCreationState.User) {
                                                   if (it) listener.enableNext()
                                                   else listener.disableNext(null)
                                                 } })
    process = VueCreateProjectProcess(projectLocation.parent, projectName, templateName, settings.myInterpreterRef,
                                      settings.myPackagePath, this)
    process!!.listener = {
      ApplicationManager.getApplication().invokeLater(
        Runnable {
          if (state != VueProjectCreationState.Process) return@Runnable

          val processState = process!!.getState()
          if (VueCreateProjectProcess.ProcessState.Error == processState.processState) {
            questionUi!!.error()
            listener.error(processState.globalProblem)
            state = VueProjectCreationState.Error
            process!!.listener = null
            process!!.cancel()
          } else if (VueCreateProjectProcess.ProcessState.QuestionsFinished == processState.processState) {
            state = VueProjectCreationState.QuestionsFinished
            listener.closeUI()
            openNewProjectAttachGenerationProgress(projectLocation)
          } else if (VueCreateProjectProcess.ProcessState.Working == processState.processState) {
            currentQuestion = processState.question
            val error = processState.question?.validationError
            if (error != null) {
              listener.disableNext(error)
              questionUi!!.activateUi()
            } else {
              listener.disableNext("")
              questionUi!!.question(processState.question!!)
            }
            state = VueProjectCreationState.User
            listener.enableNext()
          }
        }, ModalityState.any(), Condition<Boolean> { state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error })
    }
  }

  private fun openNewProjectAttachGenerationProgress(location: Path) {
    val function = Runnable {
      val projectVFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(location.toString())
      if (projectVFolder == null) {
        VueCreateProjectProcess.LOG.info(String.format("Create Vue Project: can not find project directory in '%s'", location.toString()))
      }
      else {
        RecentProjectsManager.getInstance().lastProjectCreationLocation =
          PathUtil.toSystemIndependentName(location.parent.normalize().toString())
        UsageTrigger.trigger("AbstractNewProjectStep.Vue.js")
        PlatformProjectOpenProcessor.doOpenProject(projectVFolder, null, -1, { project, _ ->
          val doneCallback = PackageJsonDependenciesExternalUpdateManager.getInstance(
            project).externalUpdateStarted(null, null)
          createListeningProgress(project, doneCallback)
        }, EnumSet.noneOf(PlatformProjectOpenProcessor.Option::class.java))
      }
    }
    ApplicationManager.getApplication().invokeLater(function, ModalityState.NON_MODAL)
  }

  private fun createListeningProgress(project: Project, doneCallback: Runnable) {
    if (process == null) return
    val task = object: Task.Backgroundable(project, "Generating Vue project...", false,
                                           PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      override fun run(indicator: ProgressIndicator) {
        process!!.waitForProcessTermination(indicator)
      }

      override fun onFinished() {
        doneCallback.run()
        JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
      }
    }
    val indicator = BackgroundableProcessIndicator(task)
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
  }

  fun getPanel(): JPanel {
    return questionUi!!.panel
  }

  fun onNext() {
    if (state == VueProjectCreationState.Process) {
      assert(false, { "should be waiting for process" })
    }
    else if (state == VueProjectCreationState.User) {
      assert(currentQuestion != null)
      listener.disableNext(null)
      if (currentQuestion!!.type == VueCreateProjectProcess.QuestionType.Checkbox) {
        val answer = questionUi!!.getCheckboxAnswer()!!
        process!!.answer(answer)
      } else {
        val answer = questionUi!!.getAnswer()!!
        process!!.answer(answer)
      }
      state = VueProjectCreationState.Process
      questionUi!!.waitForNextQuestion()
    }
    else if (state == VueProjectCreationState.QuestionsFinished || state == VueProjectCreationState.Error) {
      listener.closeUI()
    }
    else assert(false, { "Unknown state" })
  }

  override fun dispose() {
    process?.cancel() //??
  }
}

fun createVueRunningGeneratorController(projectLocation: String, settings: NpmPackageProjectGenerator.Settings,
                                        listener: VueRunningGeneratorListener): VueCliRunningGeneratorController? {
  val location = validateProjectLocation(projectLocation)
  return if (location.first != null) VueCliRunningGeneratorController(location.first!!, settings, listener)
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
  } catch (e: InvalidPathException) {
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
  fun closeUI()
}