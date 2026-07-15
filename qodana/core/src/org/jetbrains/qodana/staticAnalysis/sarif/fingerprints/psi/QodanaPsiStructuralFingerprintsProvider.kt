package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.lang.Language
import com.intellij.lang.LanguageExtension
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Language-specific PSI helper for structural fingerprint signal extraction.
 *
 * Provides structural detection (block containers, collection literals, case clauses)
 * and named scope identification (class/function declarations) used by [PsiSignalExtractor].
 *
 * Dispatched by [Language] via [LanguageExtension] — only the provider matching the
 * element's language is consulted, avoiding cross-language checks.
 */
interface QodanaPsiStructuralFingerprintsProvider {
  companion object {
    private val EP = LanguageExtension<QodanaPsiStructuralFingerprintsProvider>("org.intellij.qodana.psiStructuralFingerprintsProvider")

    fun forLanguage(language: Language): QodanaPsiStructuralFingerprintsProvider? = EP.forLanguage(language)
  }

  /** Code blocks: function bodies, if/for/while bodies, lambda bodies, try/catch bodies. */
  fun isCodeBlock(element: PsiElement): Boolean

  /** Collection literals: arrays, objects, struct initializers, dict/list/set/tuple literals. */
  fun isCollectionLiteral(element: PsiElement): Boolean = false

  /** Case/match clauses: switch cases, when entries, match arms, select comm clauses. */
  fun isCaseClause(element: PsiElement): Boolean = false

  /** Combined check — returns true if the element is any kind of block container. */
  fun isBlockContainer(element: PsiElement): Boolean =
    isCodeBlock(element) || isCollectionLiteral(element) || isCaseClause(element)

  /** Class-like declarations: classes, interfaces, structs, enums, type specs. */
  fun isClassDeclaration(element: PsiElement): Boolean = false

  /** Function-like declarations: methods, functions, constructors. */
  fun isFunctionDeclaration(element: PsiElement): Boolean = false

  /** A boundary that a serializeEnclosingElement walk stops under: file, block container, or class/function declaration. */
  fun isEnclosingScope(element: PsiElement): Boolean =
    element is PsiFile || isBlockContainer(element) || isFunctionDeclaration(element) || isClassDeclaration(element)
}
