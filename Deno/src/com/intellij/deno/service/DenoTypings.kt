package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil.execAndGetOutput
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


@Service
class DenoTypings(val project: Project) : Disposable {

  companion object {
    fun getInstance(project: Project): DenoTypings = project.getService(DenoTypings::class.java)
  }

  fun reloadAsync() {
    BackgroundTaskUtil.executeOnPooledThread(this) {
      if (reload()) {
        ApplicationManager.getApplication().invokeLater({
          val service = DenoSettings.getService(project)
          service.updateLibraries()
        }, project.disposed)
      }
    }
  }

  @Synchronized
  fun reload(): Boolean {
    val denoDirectory = getGeneratedDenoTypings()

    val commandLine = GeneralCommandLine(DenoSettings.getService(project).getDenoPath(), "types")

    val output = try {
      execAndGetOutput(commandLine)
    }
    catch (e: ExecutionException) {
      return false
    }
    val file = File(denoDirectory)
    val oldContent = try {
      FileUtil.loadFile(file)
    }
    catch (e: FileNotFoundException) {
      ""
    }
    catch (e: IOException) {
      return false
    }

    if (output.exitCode == 0) {
      val stdout = output.stdout
      if (oldContent != stdout) {
        FileUtil.writeToFile(file, stdout)
        return true
      }
    }
    return false
  }

  private fun getDenoTypings(): String {
    return FileUtil.toSystemIndependentName(getGeneratedDenoTypings())
  }

  private fun getBundledTypings(): String {
    return FileUtil.toSystemIndependentName(DenoUtil.getDenoTypings())
  }
  
  fun getDenoTypingsVirtualFile(): VirtualFile? {
    val typings = LocalFileSystem.getInstance().findFileByPath(getDenoTypings())
    if (typings != null && typings.isValid) return typings
    return LocalFileSystem.getInstance().findFileByPath(getBundledTypings())
  }

  private fun getGeneratedDenoTypings() =
    PathManager.getSystemPath() + File.separatorChar + "javascript" + File.separatorChar + "deno" + File.separatorChar + "deno.d.ts"

  override fun dispose() {}
}