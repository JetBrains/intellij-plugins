package name.kropp.intellij.makefile.toolWindow

import name.kropp.intellij.makefile.*
import java.util.Collections.*
import javax.swing.*
import javax.swing.tree.*

class MakefileRootNode(private val files: List<MakefileFileNode>) : MakefileTreeNode("make") {
  init {
    for (file in files) {
      file.parent = this
    }
  }

  override val icon: Icon
    get() = MakefileIcon

  override fun children() = enumeration(files)

  override fun isLeaf() = false

  override fun getChildCount() = files.size

  override fun getParent() = null

  override fun getChildAt(i: Int) = files[i]

  override fun getIndex(node: TreeNode) = files.indexOf(node)

  override fun getAllowsChildren() = true
}