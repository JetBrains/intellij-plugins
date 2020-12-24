package com.jetbrains.lang.makefile.toolWindow

import com.jetbrains.lang.makefile.*
import com.jetbrains.lang.makefile.psi.*
import java.util.*
import javax.swing.*
import javax.swing.tree.*

class MakefileTargetNode(val target: MakefileTarget) : MakefileTreeNode(target.name) {
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