package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.TestOnly
import java.util.*
import javax.swing.Icon
import javax.swing.tree.TreeNode

class DeviceTreeNode(private val myParent: DeviceTreeNode?,
                     val type: TYPE,
                     val name: @NlsSafe String,
                     val boardInfo: BoardInfo) : TreeNode {
  var children: MutableList<DeviceTreeNode> = mutableListOf()
  override fun getChildAt(childIndex: Int): DeviceTreeNode = children[childIndex]

  override fun getChildCount(): Int = children.size

  override fun getParent(): DeviceTreeNode? = myParent

  override fun getAllowsChildren(): Boolean = !children.isEmpty()

  override fun getIndex(node: TreeNode): Int = children.indexOf(node)

  override fun isLeaf(): Boolean = children.isEmpty()

  @TestOnly
  fun hasSameValues(name: String, type: TYPE, boardInfo: BoardInfo): Boolean =
    name == this.name && type == this.type && boardInfo == this.boardInfo

  override fun children(): Enumeration<DeviceTreeNode> = Collections.enumeration(children)

  fun add(child: DeviceTreeNode): DeviceTreeNode {
    children.add(child)
    return child
  }

  override fun toString(): String = name

  enum class TYPE(val icon: Icon?) {
    ROOT(null),
    VENDOR(AllIcons.Gutter.Colors),
    BOARD(AllIcons.Actions.GroupBy),
    FRAMEWORK(AllIcons.General.GearPlain);
  }

}
