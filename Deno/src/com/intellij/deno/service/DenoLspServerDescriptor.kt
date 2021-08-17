package com.intellij.deno.service

import com.intellij.deno.DenoUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lsp.LspServerDescriptorBase
import com.intellij.lsp.SocketModeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DenoLspServerDescriptor(project: Project, root: VirtualFile) : LspServerDescriptorBase(project, root) {
  override fun createStdioServerStartingCommandLine() = DenoUtil.getDenoExecutablePath()?.let {
    GeneralCommandLine(it, "lsp")
  } ?: throw Error("deno is not installed")

  override fun createInitializationOptions() = DenoInitializationOptions()

  override fun getSocketModeDescriptor(): SocketModeDescriptor? = null

  override fun diagnosticReceived(file: VirtualFile?) {
    // do nothing, highlighting will be requested separately
  }
}

data class DenoInitializationOptions(val enable: Boolean = true)