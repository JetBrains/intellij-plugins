package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lsp.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DenoLspSupportProvider : LspServerSupportProvider {
  override fun getServerDescriptor(project: Project, virtualFile: VirtualFile) =
    if (DenoSettings.getService(project).isUseDeno()) {
      DenoLspServerDescriptor(project, virtualFile)
    } else {
      LspServerDescriptor.emptyDescriptor()
    }
}

class DenoLspServerDescriptor(project: Project, root: VirtualFile) : LspServerDescriptorBase(project, root) {
  override fun createStdioServerStartingCommandLine() = DenoUtil.getDenoExecutablePath()?.let {
    GeneralCommandLine(it, "lsp")
  } ?: throw Error("deno is not installed")

  override fun createInitializationOptions() = DenoInitializationOptions()

  override fun getSocketModeDescriptor(): SocketModeDescriptor? = null

  override fun useGenericCompletion() = false

  override fun useGenericHighlighting() = false

  override fun useGenericNavigation() = false

  override fun equals(other: Any?) = other is DenoLspServerDescriptor && project == other.project

  override fun hashCode() = 31 * project.hashCode() + this::class.hashCode()
}

data class DenoInitializationOptions(val enable: Boolean = true, val lint: Boolean = true, val unstable: Boolean = true)