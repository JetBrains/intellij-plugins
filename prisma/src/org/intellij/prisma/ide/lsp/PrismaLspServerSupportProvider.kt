package org.intellij.prisma.ide.lsp

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lsp.api.LspServerDescriptor
import com.intellij.lsp.LspServerSupportProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerSupportProvider : LspServerSupportProvider {
  override fun getServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
    if (file.fileType != PrismaFileType) {
      return null
    }

    val node = NodeJsInterpreterManager.getInstance(project).interpreter
    if (node !is NodeJsLocalInterpreter && node !is WslNodeInterpreter) {
      return null
    }

    val fileIndex = ProjectFileIndex.getInstance(project)
    val contentRoot = fileIndex.getContentRootForFile(file) ?: return null
    for (packageJson in PackageJsonFileManager.getInstance(project).validPackageJsonFiles) {
      if (contentRoot == fileIndex.getContentRootForFile(packageJson!!)) {
        return PrismaLspServerDescriptor(project, contentRoot)
      }
    }
    return null
  }
}