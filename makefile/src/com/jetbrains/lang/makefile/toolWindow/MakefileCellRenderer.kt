package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import javax.swing.*

class MakefileCellRenderer(project: Project) : ColoredTreeCellRenderer() {
  private val rootDir: VirtualFile? = project.guessProjectDir()

  override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
    value as MakefileTreeNode
    icon = value.icon
    if (value is MakefileTargetNode && value.target.isSpecialTarget) {
      append(value.name, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES)
    }
    else {
      append(value.name)
      if (value is MakefileFileNode) {
        val file = value.psiFile.containingDirectory?.virtualFile ?: return
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