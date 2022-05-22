package com.jetbrains.lang.makefile.toolWindow

import com.intellij.psi.*
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import icons.MakefileIcons
import java.util.*
import java.util.Collections.*
import javax.swing.*
import javax.swing.tree.*

class MakefileFileNode(val psiFile: PsiFile, private val targets: List<MakefileTargetNode>) : MakefileTreeNode(psiFile.name) {
  init {
    for (target in targets) {
      target.parent = this
    }
  }

  internal lateinit var parent: MakefileRootNode

  override val icon: Icon
    get() = MakefileIcons.Makefile

  override fun children(): Enumeration<out TreeNode>? = enumeration(targets)

  override fun isLeaf() = false

  override fun getChildCount() = targets.size

  override fun getParent() = parent

  override fun getChildAt(i: Int) = targets[i]

  override fun getIndex(node: TreeNode) = targets.indexOf(node)

  override fun getAllowsChildren() = true

  override fun renderName(renderer: ColoredTreeCellRenderer) {
    super.renderName(renderer)
    renderer.append("  ${psiFile.virtualFile.path}", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES, false)
  }
}