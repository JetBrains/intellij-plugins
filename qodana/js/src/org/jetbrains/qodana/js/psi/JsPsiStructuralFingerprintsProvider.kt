package org.jetbrains.qodana.js.psi

import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder as BddTestStructureBuilder
import com.intellij.lang.javascript.psi.JSArgumentList
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
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

private const val BDD_TEST_PATH_SEPARATOR = " > "

class JsPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is JSBlockStatement
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is JSArrayLiteralExpression || element is JSObjectLiteralExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is JSCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is JSClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is JSFunction

  /**
   * The path of the BDD-style test whose callback is [scope], for example `suite > case` in
   * `describe("suite", () => it("case", () => {}))`.
   *
   * A BDD-style test has no name, only the title of its `describe`, `it` or `test` call.
   * [BddTestStructureBuilder] finds these calls. It is an alias of `JasmineFileStructureBuilder`, which covers
   * Jasmine, Mocha, Jest, Vitest and Playwright despite its name. The title must be a quoted literal, and a
   * template literal keeps its source text.
   */
  override fun anonymousScopeName(scope: PsiElement): String? {
    // Check the shape of the call first. The test structure is built per file, so it costs more.
    val call = (scope.parent as? JSArgumentList)?.parent as? JSCallExpression ?: return null
    val title = call.arguments.firstOrNull()
    if (title !is JSLiteralExpression || !title.isQuotedLiteral) return null

    val jsFile = scope.containingFile as? JSFile ?: return null
    val path = BddTestStructureBuilder.getInstance()
      .fetchCachedTestFileStructure(jsFile)
      .findTestElementPath(scope.textRange) ?: return null
    if (path.testElement !== call) return null

    return path.allNames.joinToString(BDD_TEST_PATH_SEPARATOR)
  }

  /** JS wraps an expression in parentheses, so a step skips them. */
  override fun bindingWalkParent(element: PsiElement): PsiElement? = JSUtils.getParentSkipParentheses(element)

  /** A plain name, not a qualified one, to match what the other languages report. */
  override fun declaredName(element: PsiElement): String? = when (element) {
    is JSQualifiedNamedElement -> element.name
    is JSAssignmentExpression -> element.definitionExpression?.name
    else -> null
  }
}
