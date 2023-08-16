// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.tree.TokenSet
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.impl.HCLHeredocContentMixin
import org.intellij.terraform.hcl.psi.impl.HCLPsiImplUtils

class HCLHeredocContentManipulator : AbstractElementManipulator<HCLHeredocContent>() {

  // There two major cases:
  // 1. all content replaced with actual diff very small (full heredoc injection):
  // 1.1 One line modified
  // 1.2 One line added
  // 1.3 One line removed
  // 2. one line content (probably not all) replaced with any diff (HIL injection)
  // This cases should work quite fast

  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: HCLHeredocContent, range: TextRange, newContent: String): HCLHeredocContent {
    if (range.length == 0 && element.linesCount == 0) {
      // Replace empty with something
      return replaceStupidly(element, newContent)
    }

    //////////
    // Calculate affected elements (strings) (based on offsets)

    var offset = 0
    val lines = HCLPsiImplUtils.getLinesWithEOL(element)
    val ranges = lines.map {
      val r: TextRange = TextRange.from(offset, it.length)
      offset += it.length
      r
    }

    val linesToChange = ranges.indices.filter { ranges[it].intersects(range) }
    assert(linesToChange.isNotEmpty())
    val startString: Int = linesToChange.first()
    val endString: Int = linesToChange.last()

    val node = element.node as TreeElement

    val prefixStartString = lines[startString].substring(0, range.startOffset - ranges[startString].startOffset)
    val suffixEndString = lines[endString].substring(range.endOffset - ranges[endString].startOffset)

    //////////
    // Prepare new lines content

    val newText = prefixStartString + newContent + suffixEndString
    val newLines: List<Pair<String, Boolean>> = getReplacementLines(newText)

    //////////
    // Replace nodes

    if (newLines.isNotEmpty() && !newLines.last().second && !(element.textLength == range.endOffset && !newText.endsWith("\n"))) {
      throw IllegalStateException("Last line should have line separator. New text: '$newText'\nNew lines:$newLines")
    }

    val stopNode: ASTNode? = lookupLine(node, endString)?.let { nextLine(it) }
    var iter: ASTNode? = lookupLine(node, startString)
    for ((line, _) in newLines) {
      if (iter != null && iter != stopNode) {
        // Replace existing lines
        val next = nextLine(iter)
        if (iter.isHD_EOL) {
          // Line was empty.
          if (line.isEmpty()) {
            // Left intact
          } else {
            // Add HD_LINE before 'iter' (HD_EOL)
            node.addLeaf(HCLElementTypes.HD_LINE, line, iter)
          }
        } else {
          assert(iter.elementType == HCLElementTypes.HD_LINE)
          if (line.isEmpty()) {
            // Remove HD_LINE; HD_EOL would be preserved
            node.removeChild(iter)
          } else {
            if (iter.text != line) {
              // Replace node text
              (iter as LeafElement).replaceWithText(line)
            }
          }
        }
        iter = next
      } else {
        // Add new lines to end
        if (iter == null) {
          val lcn = element.node.lastChildNode
          if (lcn != null) {
            if (lcn.isHD_EOL) {
              node.addLeaf(HCLElementTypes.HD_EOL, "\n", lcn)
              node.addLeaf(HCLElementTypes.HD_LINE, line, lcn)
            } else assert(false)
          } else {
            node.addLeaf(HCLElementTypes.HD_LINE, line, null)
            node.addLeaf(HCLElementTypes.HD_EOL, "\n", null)
          }
        } else if (stopNode != null) {
          val sNP = stopNode.treePrev
          if (sNP == null) {
            node.addLeaf(HCLElementTypes.HD_LINE, line, stopNode)
            node.addLeaf(HCLElementTypes.HD_EOL, "\n", stopNode)
          } else {
            assert(sNP.isHD_EOL)
            node.addLeaf(HCLElementTypes.HD_EOL, "\n", sNP)
            node.addLeaf(HCLElementTypes.HD_LINE, line, sNP)
          }
        } else assert(false)
      }
    }
    // Remove extra lines
    if (iter != null && iter != stopNode) {
      val iTP = iter.treePrev
      if (iTP != null && iTP.isHD_EOL) {
        if (stopNode == null) {
          node.removeRange(iTP, node.lastChildNode)
        } else {
          node.removeRange(iTP, stopNode.treePrev)
        }
      } else {
        node.removeRange(iter, stopNode)
      }
    }
    assert(node.lastChildNode?.isHD_EOL ?: true)

    check(element)
    return element
  }

  private fun check(element: HCLHeredocContent) {
    val node = element.node
    val lines = node.getChildren(TokenSet.create(HCLElementTypes.HD_LINE))
    for (line in lines) {
      assert(line.treeNext.isHD_EOL) {
        "Line HD_LINE (${line.text}) should be terminated with HD_EOL"
      }
    }
  }

  companion object {
    fun getReplacementLines(newText: String): List<Pair<String, Boolean>> {
      if (newText == "") (return emptyList())
      // TODO: Convert line separators to \n ?
      return StringUtil.splitByLinesKeepSeparators(newText).toList().map {
        it.removeSuffix("\n") to it.endsWith("\n")
      }
    }

    private fun lookupLine(node: TreeElement, n: Int): ASTNode? {
      var cn: ASTNode? = node.firstChildNode
      var counter = 0
      while (cn != null) {
        if (counter == n) return cn
        if (cn.elementType == HCLElementTypes.HD_EOL) counter++
        cn = cn.treeNext
      }
      return null
    }

    private fun nextLine(node: ASTNode): ASTNode? {
      if (node.isHD_EOL) {
        return node.treeNext
      } else {
        return node.treeNext?.treeNext
      }
    }
  }

  override fun handleContentChange(element: HCLHeredocContent, newContent: String): HCLHeredocContent {
    if (element is HCLHeredocContentMixin) {
      return handleContentChange(element, TextRange.from(0, element.textLength), newContent)
    }
    return replaceStupidly(element, newContent)
  }

  private fun replaceStupidly(element: HCLHeredocContent, newContent: String): HCLHeredocContent {
    // Do simple full replacement
    val newLines = getReplacementLines(newContent)

    val node = element.node
    if (node.firstChildNode != null) {
      node.removeRange(node.firstChildNode, null)
    }
    for ((line, _) in newLines) {
      if (line.isNotEmpty()) node.addLeaf(HCLElementTypes.HD_LINE, line, null)
      node.addLeaf(HCLElementTypes.HD_EOL, "\n", null)
    }
    return element
  }

  override fun getRangeInElement(element: HCLHeredocContent): TextRange {
    if (element.textLength == 0) return TextRange.EMPTY_RANGE
    return TextRange.from(0, element.textLength - 1)
  }
}

val ASTNode.isHD_EOL: Boolean
  get() = this.elementType == HCLElementTypes.HD_EOL


