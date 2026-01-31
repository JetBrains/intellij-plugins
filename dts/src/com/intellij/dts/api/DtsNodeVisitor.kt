package com.intellij.dts.api

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.DtsSubNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely
import com.intellij.util.containers.headTail

/**
 * Callback for [DtsNode.dtsAccept]. Visits all instances of the node and
 * resolves includes.
 */
interface DtsNodeVisitor {
  /**
   * Return true to visit the content of the node and false to skip to the
   * next instance.
   */
  fun visit(node: DtsNode): Boolean = true

  fun visitDelete() {}

  fun visitProperty(property: DtsProperty) {}

  fun visitDeleteProperty(name: String) {}

  fun visitSubNode(subNode: DtsSubNode) {}

  fun visitDeleteSubNode(name: String) {}
}

private class FileVisitor(val visitor: DtsNodeVisitor, val path: DtsPath, val forward: Boolean) : DtsFileVisitor {
  override fun visitRootNode(node: DtsRootNode, maxOffset: Int?) = visitNode(node, DtsPath.root, maxOffset)

  override fun visitRefNode(node: DtsRefNode, maxOffset: Int?) = visitNode(node, node.getDtsPath(), maxOffset)

  override fun visitDeleteNode(node: DtsNode) {
    val nodePath = node.getDtsPath() ?: return

    if (nodePath.relativize(path) != null) visitor.visitDelete()
  }

  private fun visitNode(node: DtsNode, nodePath: DtsPath?, maxOffset: Int?) {
    if (nodePath == null) return
    val relativePath = nodePath.relativize(path) ?: return

    searchNode(node, relativePath.segments, maxOffset)
  }

  private fun visitCompilerDirective(directive: DtsCompilerDirective) {
    val arg = directive.dtsDirectiveArgs.firstOrNull() ?: return
    if (arg.elementType != DtsTypes.NAME) return

    when (directive.dtsDirectiveType) {
      DtsTypes.DELETE_PROP -> visitor.visitDeleteProperty(arg.text)
      DtsTypes.DELETE_NODE -> visitor.visitDeleteSubNode(arg.text)
    }
  }

  private fun walkContent(node: DtsNode) {
    val walkContent = visitor.visit(node)
    if (!walkContent) return

    val content = node.dtsContent ?: return
    for (statement in content.dtsStatements) {
      when (statement) {
        is DtsCompilerDirective -> visitCompilerDirective(statement)
        is DtsSubNode -> visitor.visitSubNode(statement)
        is DtsProperty -> visitor.visitProperty(statement)
        else -> {}
      }
    }
  }

  private fun walkDelete(directive: DtsCompilerDirective, name: String) {
    if (directive.dtsDirectiveType != DtsTypes.DELETE_NODE) return

    val arg = directive.dtsDirectiveArgs.firstOrNull() ?: return
    if (arg.elementType != DtsTypes.NAME || arg.text != name) return

    visitor.visitDelete()
  }

  private fun searchNode(node: DtsNode, segments: List<String>, maxOffset: Int?) {
    if (segments.isEmpty()) {
      walkContent(node)
      return
    }

    var statements = node.dtsContent?.dtsStatements ?: return
    val (head, tail) = segments.headTail()

    if (!forward) {
      statements = statements.reversed()
    }

    for (statement in statements) {
      if (afterMaxOffset(statement, maxOffset)) return

      if (statement is DtsSubNode && statement.dtsName == head) {
        searchNode(statement, tail, maxOffset)
      }

      if (tail.isEmpty() && statement is DtsCompilerDirective) {
        walkDelete(statement, head)
      }
    }
  }
}

fun DtsFile.dtsAccept(visitor: DtsNodeVisitor, path: DtsPath, forward: Boolean = true, maxOffset: Int? = null): Boolean {
  return dtsAccept(FileVisitor(visitor, path, forward), forward, maxOffset)
}

fun DtsNode.dtsAccept(visitor: DtsNodeVisitor, forward: Boolean = true, strict: Boolean = false): Boolean {
  val file = containingFile.asSafely<DtsFile>() ?: return false
  val path = getDtsPath() ?: return false

  return file.dtsAccept(
    visitor,
    path,
    forward = forward,
    maxOffset = if (strict) node.startOffset else node.startOffset + 1,
  )
}