package com.jetbrains.lang.makefile.toolWindow

import com.jetbrains.lang.makefile.MakefileTargetIcon
import com.jetbrains.lang.makefile.psi.MakefileTarget
import java.util.Enumeration
import javax.swing.Icon
import javax.swing.tree.TreeNode

class MakefileTargetNode(target: MakefileTarget) : MakefileTreeNode(target.name) {
  val isSpecialTarget: Boolean = target.isSpecialTarget

  override val icon: Icon
    get() = MakefileTargetIcon

  internal lateinit var parent: MakefileFileNode

  override fun children(): Enumeration<out TreeNode>? = null

  override fun isLeaf() = true

  override fun getChildCount() = 0

  override fun getParent() = parent

  override fun getChildAt(i: Int) = null

  override fun getIndex(node: TreeNode) = 0

  override fun getAllowsChildren() = false
}