package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals

private const val AST_SHAPE_DEPTH = 2
private const val DIRECT_PSI_ELEMENT_ONLY_DEPTH = 1

private data class EnclosingScopeNameAndType(val enclosingScopeName: String?, val enclosingScopeType: String?) {
  companion object {
    val EMPTY = EnclosingScopeNameAndType(null, null)
  }
}

/** Language-agnostic extraction engine for PSI structural signals. */
object PsiSignalExtractor {
  fun extractSignals(element: PsiElement): StructuralFingerprintSignals {
    val provider = QodanaPsiStructuralFingerprintsProvider.forLanguage(element.language)
    val (enclosingScopeName, enclosingScopeType) = extractEnclosingScopeNameAndType(element, provider)
    val astShape = serializeEnclosingElement(element, provider)
    return StructuralFingerprintSignals(astShape, enclosingScopeName, enclosingScopeType)
  }

  /**
   * Single walk up the PSI tree producing:
   * - **enclosingScopeName**: nearest enclosing function-like declaration name (via [QodanaPsiStructuralFingerprintsProvider.isFunctionDeclaration])
   * - **enclosingScopeType**: type of the nearest enclosing function-like statement.
   */
  private fun extractEnclosingScopeNameAndType(element: PsiElement, provider: QodanaPsiStructuralFingerprintsProvider?): EnclosingScopeNameAndType {
    if (provider == null) return EnclosingScopeNameAndType.EMPTY
    val enclosingScope = generateSequence(element) { it.parent }
      .takeWhile { it !is PsiFile }
      .firstOrNull { provider.isFunctionDeclaration(it) }

    return EnclosingScopeNameAndType(
      (enclosingScope as? PsiNamedElement)?.name,
      enclosingScope?.let { elementTypeName(it) }
    )
  }

  /**
   * Walk up the PSI tree from [element] (up to [AST_SHAPE_DEPTH] levels) to find the
   * nearest enclosing statement, and serialize its structural shape via [serializeTree].
   *
   * - If the problem is a function or class statement → serialize the original
   *   [element] at [DIRECT_PSI_ELEMENT_ONLY_DEPTH] as a fallback,
   *   otherwise an astShape may become too big and structurally fragile.
   */
  private fun serializeEnclosingElement(
    element: PsiElement,
    provider: QodanaPsiStructuralFingerprintsProvider?,
    maxDepth: Int = AST_SHAPE_DEPTH
  ): String {
    if (provider == null) return serializeTree(element, DIRECT_PSI_ELEMENT_ONLY_DEPTH)

    var current = element

    repeat(maxDepth) {
      val parent = current.parent

      if (parent == null || provider.isFunctionDeclaration(current) || provider.isClassDeclaration(current)) {
        return serializeTree(current, DIRECT_PSI_ELEMENT_ONLY_DEPTH)
      }

      if (provider.isEnclosingScope(parent)) {
        return serializeTree(current, maxDepth)
      }

      current = parent
    }

    return serializeTree(current, DIRECT_PSI_ELEMENT_ONLY_DEPTH)
  }

  /**
   * Serialize a PSI subtree using raw IElementType names, skipping whitespace, comments, and punctuation.
   * Produces expressions like: IF_STATEMENT(SIMPLE_STATEMENT(LEFT_HAND_EXPR_LIST),BLOCK(SIMPLE_STATEMENT))
   *
   * Used for [StructuralFingerprintSignals.astShape]: full depth on the enclosing statement when one exists,
   * or a depth-limited serialization of the problem element itself when it sits outside any block.
   */
  internal fun serializeTree(element: PsiElement, maxDepth: Int = Int.MAX_VALUE): String {
    val typeName = elementTypeName(element) ?: return ""
    if (maxDepth <= 0) return typeName

    val parts = generateSequence(element.firstChild) { it.nextSibling }
      .filterNot { it is PsiFile || it is PsiWhiteSpace || it is PsiComment || isLeafPunctuation(it) }
      .mapNotNull { child ->
        serializeTree(child, maxDepth - 1).ifEmpty { null }
      }
      .toList()

    return if (parts.isEmpty()) typeName else "$typeName(${parts.joinToString(",")})"
  }

  /** Leaf tokens like `{`, `}`, `;` that carry no structural information. */
  private fun isLeafPunctuation(element: PsiElement): Boolean =
    element.children.isEmpty() && element.textLength <= 1

  private fun elementTypeName(element: PsiElement): String? =
    PsiUtilCore.getElementType(element)?.toString()
}
