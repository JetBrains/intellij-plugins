package name.kropp.intellij.makefile.toolWindow

import com.intellij.util.enumeration.*
import name.kropp.intellij.makefile.*
import javax.swing.*
import javax.swing.tree.*

class MakefileRootNode(private val files: Array<MakefileFileNode>) : MakefileTreeNode("make") {
  init {
    for (file in files) {
      file.parent = this
    }
  }

  override val icon: Icon
    get() = MakefileIcon

  override fun children() = ArrayEnumeration(files)

  override fun isLeaf() = false

  override fun getChildCount() = files.size

  override fun getParent() = null

  override fun getChildAt(i: Int) = files[i]

  override fun getIndex(node: TreeNode) = files.indexOf(node)

  override fun getAllowsChildren() = true
}