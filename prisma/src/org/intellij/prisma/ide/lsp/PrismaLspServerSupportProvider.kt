package org.intellij.prisma.ide.lsp

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lsp.api.LspServerSupportProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
    if (file.fileType != PrismaFileType) return

    val node = NodeJsInterpreterManager.getInstance(project).interpreter
    if (node !is NodeJsLocalInterpreter && node !is WslNodeInterpreter) return

    val fileIndex = ProjectFileIndex.getInstance(project)
    val contentRoot = fileIndex.getContentRootForFile(file) ?: return
    for (packageJson in PackageJsonFileManager.getInstance(project).validPackageJsonFiles) {
      if (contentRoot == fileIndex.getContentRootForFile(packageJson)) {
        serverStarter.ensureServerStarted(PrismaLspServerDescriptor(project, contentRoot))
      }
    }
  }
}