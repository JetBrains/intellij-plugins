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

  /**
   * Function-like declarations: a method, a function, a constructor, a property accessor, an initializer and
   * an anonymous function. Each language decides which of these it has.
   */
  fun isFunctionDeclaration(element: PsiElement): Boolean = false

  /** Any scope: a function-like or a class-like declaration. */
  fun isScope(element: PsiElement): Boolean = isFunctionDeclaration(element) || isClassDeclaration(element)

  /** A label for an anonymous [scope], for example `Holder` for a Kotlin `init { }`. */
  fun anonymousScopeName(scope: PsiElement): String? = null

  /** The name that [element] declares, for example a variable or the target of an assignment. */
  fun declaredName(element: PsiElement): String? = null

  /** One step outward for the binding walk. Override it to skip a wrapper such as a parenthesis. */
  fun bindingWalkParent(element: PsiElement): PsiElement? = element.parent

  /** A boundary that a serializeEnclosingElement walk stops under: a file, a block container or a scope. */
  fun isEnclosingScope(element: PsiElement): Boolean =
    element is PsiFile || isBlockContainer(element) || isScope(element)
}
