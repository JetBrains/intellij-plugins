package org.intellij.prisma.ide.lsp

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lsp.LspServerDescriptor
import com.intellij.lsp.LspServerSupportProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

class PrismaLspServerSupportProvider : LspServerSupportProvider {
  override fun getServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor {
    val node = NodeJsInterpreterManager.getInstance(project).interpreter
    if (node !is NodeJsLocalInterpreter && node !is WslNodeInterpreter) {
      return LspServerDescriptor.emptyDescriptor()
    }
    val fileIndex = ProjectFileIndex.getInstance(project)
    val contentRoot = fileIndex.getContentRootForFile(file) ?: return LspServerDescriptor.emptyDescriptor()
    for (packageJson in PackageJsonFileManager.getInstance(project).validPackageJsonFiles) {
      if (contentRoot == fileIndex.getContentRootForFile(packageJson!!)) {
        return PrismaLspServerDescriptor(project, contentRoot)
      }
    }
    return LspServerDescriptor.emptyDescriptor()
  }
}