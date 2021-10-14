package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil.execAndGetOutput
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.io.IOException


@Service
class DenoTypings(val project: Project) {

  companion object {
    fun getInstance(project: Project): DenoTypings = project.getService(DenoTypings::class.java)
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

  fun getDenoTypings(): String {
    return FileUtil.toSystemIndependentName(getGeneratedDenoTypings())
  }

  fun getBundledTypings(): String {
    return DenoUtil.getDenoTypings()
  }

  private fun getGeneratedDenoTypings() =
    PathManager.getSystemPath() + File.separatorChar + "javascript" + File.separatorChar + "deno" + File.separatorChar + "deno.d.ts"
}