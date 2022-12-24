package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.build.BuildContentManager
import com.intellij.build.BuildViewManager
import com.intellij.build.events.MessageEvent
import com.intellij.build.progress.BuildProgress
import com.intellij.build.progress.BuildProgressDescriptor
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.ide.impl.isTrusted
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectModelExternalSource
import com.intellij.openapi.util.Key
import com.intellij.task.*
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.LOG
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatfromioCliBuilder
import com.jetbrains.cidr.cpp.execution.build.runners.CLionCompileResolveConfigurationTaskRunner
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.execution.build.runners.CidrProjectTaskRunner
import com.jetbrains.cidr.execution.build.runners.CidrTaskRunner
import com.jetbrains.cidr.execution.build.tasks.CidrCleanTask
import org.jetbrains.annotations.Nls
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import java.nio.file.Path

const val EXECUTION_TIMEOUT_MS = 10 * 3600 * 1000 /*10 hrs*/

class PlatformioProjectTaskRunner : CidrProjectTaskRunner() {
  override val buildSystemId: String = ID.id
  override fun canRun(project: Project, projectTask: ProjectTask): Boolean {
    return project.service<PlatformioWorkspace>().isInitialized
           && project.isTrusted() && canRun(projectTask)
  }

  override fun canRun(task: ProjectTask): Boolean {
    return when (task) {
      is PlatformioTargetTask -> true
      is ProjectModelBuildTask<*> -> task.buildableElement == PlatformioBuildConfiguration
      is ModuleFilesBuildTask -> ExternalSystemApiUtil.isExternalSystemAwareModule(ID, task.module)
      is ModuleBuildTask -> ExternalSystemApiUtil.isExternalSystemAwareModule(ID, task.module)
      is CidrCleanTask -> task.buildConfiguration == PlatformioBuildConfiguration
      else -> false
    }
  }

  override fun runnerForTask(task: ProjectTask, project: Project): CidrTaskRunner? {
    return when (task) {
      is ModuleFilesBuildTask -> PlatformioCompileTaskRunner
      is ModuleBuildTask, is CidrCleanTask, is ProjectModelBuildTask<*>, is PlatformioTargetTask -> PlatformioTaskRunner()
      else -> null
    }
  }

}

class PlatformioTaskRunner : CidrTaskRunner {
  override fun executeTask(project: Project,
                           task: ProjectTask,
                           sessionId: Any,
                           context: ProjectTaskContext): Promise<ProjectTaskRunner.Result> {
    val promise = AsyncPromise<ProjectTaskRunner.Result>()
    val buildProgress = BuildViewManager.createBuildProgress(project)
    val basePath = project.basePath
    if (basePath == null) {
      fail(buildProgress, promise)
      return promise
    }
    invokeAndWaitIfNeeded {
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    try {
      val compilerCommandLine = PlatfromioCliBuilder(project, true).withParams("run")

      @Nls val title: String
      when (task) {
        is CidrCleanTask -> {
          compilerCommandLine.withParams("-t", "clean")
          title = ClionEmbeddedPlatformioBundle.message("platformio.clean")
        }
        is PlatformioTargetTask -> {
          compilerCommandLine.withParams(*task.args)
          title = task.presentableName
        }
        else -> title = ClionEmbeddedPlatformioBundle.message("platformio.build")
      }

      val processHandler = CapturingProcessHandler(compilerCommandLine.build())
      processHandler.addProcessListener(object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
          buildProgress.output(event.text, outputType == ProcessOutputTypes.STDOUT)
        }
      })

      val indicator = EmptyProgressIndicator()
      val buildDescriptor = PlatformioBuildDescriptor(project, title, basePath, processHandler, indicator, buildProgress)

      buildProgress.start(buildDescriptor)
      ApplicationManager.getApplication().invokeLater {
        BuildContentManager.getInstance(project).getOrCreateToolWindow().activate(null, false)
      }

      val processOutput = processHandler.runProcessWithProgressIndicator(indicator)
      if (processOutput.exitCode == 0 && !processOutput.isTimeout) {
        buildProgress.finish()
        promise.setResult(TaskRunnerResults.SUCCESS)
      }
      else if (processOutput.isTimeout || processOutput.isCancelled) {
        buildProgress.cancel()
        promise.setResult(TaskRunnerResults.ABORTED)
      }
      else {
        fail(buildProgress, promise)
      }

    }
    catch (e: ProcessCanceledException) {
      buildProgress.fail()
      promise.setResult(TaskRunnerResults.ABORTED)
    }
    catch (e: Throwable) {
      LOG.warn(e)
      buildProgress.message(ClionEmbeddedPlatformioBundle.message("build.event.title.exec.fail"), e.message ?: "",
                            MessageEvent.Kind.ERROR,
                            null)
      fail(buildProgress, promise)
    }
    return promise
  }

  companion object {
    private fun fail(buildProgress: BuildProgress<BuildProgressDescriptor>,
                     promise: AsyncPromise<ProjectTaskRunner.Result>) {
      buildProgress.fail()
      promise.setResult(TaskRunnerResults.FAILURE)
    }
  }
}

object PlatformioCompileTaskRunner : CLionCompileResolveConfigurationTaskRunner() {
  override fun projectDir(project: Project): Path =
    project.service<PlatformioWorkspace>().projectPath

  override fun canRun(project: Project): Boolean =
    project.service<PlatformioWorkspace>().isInitialized

  override fun environment(project: Project): CPPEnvironment =
    project.service<PlatformioWorkspace>().environment

  override fun externalSource(project: Project): ProjectModelExternalSource =
    object : ProjectModelExternalSource {
      override fun getDisplayName(): String =
        ID.readableName

      override fun getId(): String =
        ID.id
    }
}
