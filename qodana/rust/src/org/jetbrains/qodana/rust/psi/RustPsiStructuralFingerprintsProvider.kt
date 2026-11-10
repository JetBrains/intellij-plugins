package org.jetbrains.qodana.rust.psi

import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider
import org.rust.lang.core.psi.RsArrayExpr
import org.rust.lang.core.psi.RsBinaryExpr
import org.rust.lang.core.psi.RsBlock
import org.rust.lang.core.psi.RsConstant
import org.rust.lang.core.psi.RsFieldLookupExpr
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsLambdaExpr
import org.rust.lang.core.psi.RsLetDecl
import org.rust.lang.core.psi.RsMatchArm
import org.rust.lang.core.psi.RsMembers
import org.rust.lang.core.psi.RsModItem
import org.rust.lang.core.psi.RsPatIdent
import org.rust.lang.core.psi.RsPathExpr
import org.rust.lang.core.psi.RsPathType
import org.rust.lang.core.psi.RsStructLiteralBody
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.RsVecMacroArgument
import org.rust.lang.core.psi.ext.RsStructOrEnumItemElement

class RustPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  /** [RsMembers] lists every member of an `impl` or a trait, so the walk stops at it. */
  override fun isCodeBlock(element: PsiElement): Boolean = element is RsBlock || element is RsMembers

  /** [RsVecMacroArgument] holds the elements of a `vec![]`, a collection literal like `[]`. */
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is RsStructLiteralBody || element is RsArrayExpr || element is RsVecMacroArgument

  override fun isCaseClause(element: PsiElement): Boolean = element is RsMatchArm

  /** A struct, an enum, a trait, an `impl` and a `mod`. Each can hold a constant outside any function. */
  override fun isClassDeclaration(element: PsiElement): Boolean =
    element is RsStructOrEnumItemElement || element is RsTraitItem ||
    element is RsImplItem || element is RsModItem

  /**
   * The type that labels an unnamed `impl`, such as `Foo` for `impl Display for Foo`.
   *
   * The name alone, without the type arguments, so a rename of `T` in `impl<T> Wrapper<T>` changes nothing.
   */
  override fun anonymousScopeName(scope: PsiElement): String? = implTypeName(scope)

  private fun implTypeName(scope: PsiElement): String? {
    val typeReference = (scope as? RsImplItem)?.typeReference ?: return null
    return (typeReference as? RsPathType)?.path?.identifier?.text ?: typeReference.text
  }

  /** A `fn`, a method, a trait method and a closure. A closure with no name stays unnamed, never hidden. */
  override fun isFunctionDeclaration(element: PsiElement): Boolean =
    element is RsFunction || element is RsLambdaExpr

  /** The name that `let scale = |x| x;`, `scale = |x| x;` or `const SCALE: F = |x| x;` binds. */
  override fun declaredName(element: PsiElement): String? = when (element) {
    is RsLetDecl -> (element.pat as? RsPatIdent)?.patBinding?.name
    is RsConstant -> element.name
    is RsBinaryExpr -> assignedName(element)
    else -> null
  }

  /**
   * The plain name an assignment writes to. `self.factor = |x| x;` gives `factor`, as Go and Java report it.
   *
   * Only `=` binds a name. A `+=` reads the target and writes it back, so it labels nothing.
   */
  private fun assignedName(assignment: RsBinaryExpr): String? {
    if (assignment.binaryOp.eq == null) return null

    return when (val target = assignment.left) {
      is RsPathExpr -> target.path.identifier?.text
      is RsFieldLookupExpr -> target.fieldLookup.identifier?.text
      else -> null
    }
  }
}
