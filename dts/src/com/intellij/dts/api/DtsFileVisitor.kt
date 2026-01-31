package com.intellij.dts.api

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.psi.PsiFileInclude
import com.intellij.dts.lang.resolve.files.DtsOverlayFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import com.intellij.util.asSafely

class DtsVisitorCanceledException : Exception()

/**
 * Callback for [DtsFile.dtsAccept]. Visits all root level entries and resolves
 * includes.
 */
interface DtsFileVisitor {
  /**
   * Return true to visit the content of the file and false to skip to the
   * next sibling.
   */
  fun visit(file: DtsFile): Boolean = true

  fun visitInclude(file: DtsFile, include: FileInclude) {}

  fun visitDeleteNode(node: DtsNode) {}

  fun visitRootNode(node: DtsRootNode, maxOffset: Int?) {}

  fun visitRefNode(node: DtsRefNode, maxOffset: Int?) {}
}

internal fun afterMaxOffset(element: PsiElement, maxOffset: Int?): Boolean {
  return maxOffset != null && element.startOffset >= maxOffset
}

private abstract class Walker(val visitor: DtsFileVisitor) {
  val visited = mutableSetOf<PsiFile>()

  protected abstract fun walkContent(file: DtsFile, maxOffset: Int?)

  private fun alreadyVisited(file: DtsFile): Boolean {
    return !visited.add(file)
  }

  fun walkFile(file: DtsFile, maxOffset: Int?) {
    if (alreadyVisited(file)) return

    val walkContent = visitor.visit(file)
    if (!walkContent) return

    walkContent(file, maxOffset)
  }

  protected fun walkInclude(file: DtsFile, include: FileInclude) {
    visitor.visitInclude(file, include)

    val target = include.resolve(file) ?: return
    if (target !is DtsFile) return

    walkFile(target, null)
  }

  private fun walkCompilerDirective(directive: DtsCompilerDirective, maxOffset: Int?) {
    if (directive.dtsDirectiveType != DtsTypes.DELETE_NODE) return

    val arg = directive.dtsDirectiveArgs.firstOrNull() ?: return
    if (arg !is DtsPHandle || afterMaxOffset(arg, maxOffset)) return

    val target = arg.reference?.resolve().asSafely<DtsNode>() ?: return
    visitor.visitDeleteNode(target)
  }

  private fun walkEntry(entry: DtsEntry, maxOffset: Int?) {
    when (val statement = entry.dtsStatement) {
      is DtsRootNode -> visitor.visitRootNode(statement, maxOffset)
      is DtsRefNode -> visitor.visitRefNode(statement, maxOffset)
      is DtsCompilerDirective -> walkCompilerDirective(statement, maxOffset)
      else -> {}
    }
  }

  protected fun walkChild(file: DtsFile, child: PsiElement, maxOffset: Int?) {
    if (afterMaxOffset(child, maxOffset)) return

    if (child is PsiFileInclude) {
      child.fileInclude?.let { include -> walkInclude(file, include) }
    }

    if (child is DtsEntry) {
      walkEntry(child, maxOffset)
    }
  }
}

private class WalkerForward(visitor: DtsFileVisitor) : Walker(visitor) {
  override fun walkContent(file: DtsFile, maxOffset: Int?) {
    if (file is DtsFile.Overlay) {
      walkInclude(file, DtsOverlayFile)
    }

    for (child in file.children) {
      walkChild(file, child, maxOffset)
    }
  }
}

private class WalkerBackward(visitor: DtsFileVisitor) : Walker(visitor) {
  override fun walkContent(file: DtsFile, maxOffset: Int?) {
    for (child in file.children.reversed()) {
      walkChild(file, child, maxOffset)
    }

    if (file is DtsFile.Overlay) {
      walkInclude(file, DtsOverlayFile)
    }
  }
}

/**
 * Walks the files content. Returns true if a [DtsVisitorCanceledException] was
 * thrown otherwise false.
 *
 * @param forward If true walks the file from top to bottom from bottom to top.
 * @param maxOffset If set ignores all nodes which have a higher or equal startOffset.
 */
fun DtsFile.dtsAccept(visitor: DtsFileVisitor, forward: Boolean = true, maxOffset: Int? = null): Boolean {
  val walker = if (forward) WalkerForward(visitor) else WalkerBackward(visitor)

  try {
    walker.walkFile(this, maxOffset)
    return false
  }
  catch (e: DtsVisitorCanceledException) {
    return true
  }
}
