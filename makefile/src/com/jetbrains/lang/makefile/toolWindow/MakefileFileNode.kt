package com.jetbrains.lang.makefile.toolWindow

import com.intellij.psi.PsiFile
import icons.MakefileIcons
import java.util.Collections.enumeration
import java.util.Enumeration
import javax.swing.Icon
import javax.swing.tree.TreeNode

class MakefileFileNode(private val myPsiFile: PsiFile, private val targets: List<MakefileTargetNode>) : MakefileTreeNode(myPsiFile.name) {
  init {
    for (target in targets) {
      target.parent = this
    }
  }

  val psiFile: PsiFile? get() { return if (myPsiFile.isValid) myPsiFile else null }

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
}