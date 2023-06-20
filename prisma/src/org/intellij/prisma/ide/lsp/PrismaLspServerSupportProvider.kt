package org.intellij.prisma.ide.lsp

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
    if (file.fileType != PrismaFileType) return
    if (NodeJsInterpreterManager.getInstance(project).interpreter !is NodeJsLocalInterpreter) return

    serverStarter.ensureServerStarted(PrismaLspServerDescriptor(project))
  }
}