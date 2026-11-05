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
    val problemElementType = elementTypeName(element)?.ifEmpty { null }
    return StructuralFingerprintSignals(astShape, enclosingScopeName, enclosingScopeType, problemElementType)
  }

  /**
   * Single walk up the PSI tree producing:
   * - **enclosingScopeName**: name of the nearest enclosing scope that yields a name
   * - **enclosingScopeType**: type of that same scope.
   *
   * A scope is a function-like or a class-like declaration. So a problem in a class body, an interface or a
   * TypeScript type alias also gets a name. The nearest scope wins, whichever kind it is.
   *
   * A scope that yields no name is skipped, and the walk continues outward. An anonymous function therefore
   * reports the label that [QodanaPsiStructuralFingerprintsProvider.anonymousScopeName] derives, or the name
   * of a scope further out.
   */
  private fun extractEnclosingScopeNameAndType(
    element: PsiElement,
    provider: QodanaPsiStructuralFingerprintsProvider?,
  ): EnclosingScopeNameAndType {
    if (provider == null) return EnclosingScopeNameAndType.EMPTY

    return generateSequence(element) { it.parent }
      .takeWhile { it !is PsiFile }
      .filter(provider::isScope)
      .firstNotNullOfOrNull { scope ->
        scopeName(scope, provider)?.let { EnclosingScopeNameAndType(it, elementTypeName(scope)) }
      }
      ?: EnclosingScopeNameAndType.EMPTY
  }

  /** The declared name of [scope], else a label from the provider, else the nearest binding around it. */
  private fun scopeName(scope: PsiElement, provider: QodanaPsiStructuralFingerprintsProvider): String? =
    elementName(scope)?.ifBlank { null }
    ?: provider.anonymousScopeName(scope)?.ifBlank { null }
    ?: bindingName(scope, provider)

  /**
   * The nearest binding around an anonymous [scope], for example `r` in `Runnable r = () -> { }`.
   *
   * The walk passes any expression, statement or block, and stops at the next scope.
   */
  private fun bindingName(scope: PsiElement, provider: QodanaPsiStructuralFingerprintsProvider): String? =
    generateSequence(provider.bindingWalkParent(scope)) { provider.bindingWalkParent(it) }
      .takeWhile { it !is PsiFile && !provider.isScope(it) }
      .firstNotNullOfOrNull { provider.declaredName(it)?.ifBlank { null } }

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

      if (parent == null || provider.isScope(current)) {
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

  /** The declared name of [element], or null when it has none, like an anonymous JS callback or a PHP closure. */
  private fun elementName(element: PsiElement): String? = (element as? PsiNamedElement)?.name

  private fun elementTypeName(element: PsiElement): String? =
    PsiUtilCore.getElementType(element)?.toString()
}
