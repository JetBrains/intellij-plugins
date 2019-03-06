package name.kropp.intellij.makefile.toolWindow

import com.intellij.psi.*
import name.kropp.intellij.makefile.*
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
    get() = MakefileIcon

  override fun children(): Enumeration<*> = enumeration(targets)

  override fun isLeaf() = false

  override fun getChildCount() = targets.size

  override fun getParent() = parent

  override fun getChildAt(i: Int) = targets[i]

  override fun getIndex(node: TreeNode) = targets.indexOf(node)

  override fun getAllowsChildren() = true
}