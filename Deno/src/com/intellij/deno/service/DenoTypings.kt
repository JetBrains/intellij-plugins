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
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path


@Service(Service.Level.PROJECT)
class DenoTypings(val project: Project) : Disposable {

  companion object {
    fun getInstance(project: Project): DenoTypings = project.getService(DenoTypings::class.java)
  }

  fun reloadAsync() {
    BackgroundTaskUtil.executeOnPooledThread(this) {
      if (saveDenoTypings()) {
        ApplicationManager.getApplication().invokeLater({
                                                          val service = DenoSettings.getService(project)
                                                          service.updateLibraries()
                                                        }, project.disposed)
      }
    }
  }

  @Synchronized
  fun saveDenoTypings(): Boolean {
    val denoDirectory = getGeneratedDenoTypings()
    val commandLine = GeneralCommandLine(DenoSettings.getService(project).getDenoPath(), "types")

    val output = try {
      execAndGetOutput(commandLine)
    }
    catch (_: ExecutionException) {
      return false
    }

    if (output.exitCode != 0) return false
    val file = Path.of(FileUtil.toSystemDependentName(denoDirectory))
    val oldContent = try {
      Files.readString(file)
    }
    catch (_: IOException) {
      ""
    }

    val stdout = output.stdout
    if (oldContent == stdout) return false

    try {
      Files.createDirectories(file.parent)
      Files.writeString(file, stdout)
    }
    catch (e: IOException) {
      logger<DenoTypings>().warn(e)
    }
    return true
  }

  private fun getDenoTypings(): String {
    return FileUtil.toSystemIndependentName(getGeneratedDenoTypings())
  }

  private fun getBundledTypings(): String {
    return FileUtil.toSystemIndependentName(DenoUtil.getDenoTypings())
  }

  fun isDenoTypings(virtualFile: VirtualFile): Boolean {
    val path = virtualFile.path
    return path == getDenoTypings() || path == getBundledTypings()
  }

  fun getDenoTypingsVirtualFile(): VirtualFile? {
    val typings = LocalFileSystem.getInstance().findFileByPath(getDenoTypings())
    if (typings != null && typings.isValid) return typings
    return LocalFileSystem.getInstance().findFileByPath(getBundledTypings())
  }

  private fun getGeneratedDenoTypings() =
    "${PathManager.getSystemPath()}/javascript/deno/deno.d.ts"

  override fun dispose() {}
}