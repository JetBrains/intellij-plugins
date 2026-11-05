package org.jetbrains.qodana.js.psi

import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.lang.javascript.psi.JSBlockStatement
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSCaseClause
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class JsPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is JSBlockStatement
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is JSArrayLiteralExpression || element is JSObjectLiteralExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is JSCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is JSClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is JSFunction

  /** A label for an anonymous function: the title of the test around it, else the nearest binding. */
  override fun anonymousScopeName(scope: PsiElement): String? = testTitle(scope) ?: bindingName(scope)

  /**
   * The title of the test or the suite that holds [scope], for example `case` in `it("case", () => {})`.
   *
   * Reuses the IDE test structure, so the recognised callees stay in one place. It covers `describe`, `it`,
   * `test`, the `x`/`f` forms and the `.skip`, `.only` and `.each` modifiers.
   */
  private fun testTitle(scope: PsiElement): String? {
    val jsFile = scope.containingFile as? JSFile ?: return null
    val path = JasmineFileStructureBuilder.getInstance()
      .fetchCachedTestFileStructure(jsFile)
      .findTestElementPath(scope.textRange) ?: return null

    val title = (path.testElement as? JSCallExpression)?.arguments?.firstOrNull()
    if (title !is JSLiteralExpression || !title.isStringLiteral) return null

    return path.allNames.lastOrNull()?.takeIf(String::isNotBlank)
  }

  /** The nearest binding around an anonymous function, for example `Nu` in `Nu = (() => {})()`. */
  private fun bindingName(scope: PsiElement): String? =
    generateSequence(JSUtils.getParentSkipParentheses(scope)) { JSUtils.getParentSkipParentheses(it) }
      .takeWhile { it !is PsiFile && !isFunctionDeclaration(it) }
      .firstNotNullOfOrNull { declaredName(it) }

  /** The name a binding declares. A plain name, not a qualified one, to match what the other languages report. */
  private fun declaredName(element: PsiElement): String? = when (element) {
    is JSQualifiedNamedElement -> element.name
    is JSAssignmentExpression -> element.definitionExpression?.name
    else -> null
  }
}
