package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.SlowOperations
import javax.swing.JTree

class MakefileCellRenderer(private val project: Project) : ColoredTreeCellRenderer() {
  private val rootDir: VirtualFile? = project.guessProjectDir()

  override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
    value as MakefileTreeNode
    icon = value.icon
    if (value is MakefileTargetNode && ReadAction.compute<Boolean, Exception> { value.isSpecialTarget }) {
      append(value.name, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
    }
    else {
      append(value.name)
      if (value is MakefileFileNode && !project.isDisposed) {
        val file = SlowOperations.knownIssue("CPP-41044").use {
          ReadAction.compute<VirtualFile, Exception> {
            value.psiFile?.containingDirectory?.virtualFile
          } ?: return
        }
        if (rootDir != null) {
          val relativePath = VfsUtilCore.getRelativePath(file, rootDir) ?: file.path
          if (relativePath.isBlank()) {
            return
          }
          append(" ")
          append(relativePath, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
      }
    }
  }
}