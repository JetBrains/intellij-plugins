package org.angular2.library.forms.scopes

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.angular2.library.forms.Angular2FormAbstractControl
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.impl.Angular2UnknownFormGroup

class Angular2FormGroupGetCallArrayLiteralScope(private val formGroup: Angular2FormGroup, private val location: JSExpression) :
  PolySymbolScope {

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_STRING_LITERALS

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (kind == JS_STRING_LITERALS)
      listOf(formGroupGetPathRefSymbol)
    else
      findFormSymbol()?.getSymbols(kind, params, stack)
      ?: emptyList()

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    if (qualifiedName.kind == JS_STRING_LITERALS)
      super.getCodeCompletions(qualifiedName, params, stack)
    else
      findFormSymbol()?.getCodeCompletions(qualifiedName, params, stack)
      ?: emptyList()

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.kind == JS_STRING_LITERALS)
      super.getMatchingSymbols(qualifiedName, params, stack)
    else
      findFormSymbol()?.getMatchingSymbols(qualifiedName, params, stack)
      ?: emptyList()

  override fun createPointer(): Pointer<out PolySymbolScope> {
    val formGroupPtr = formGroup.createPointer()
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val formGroup = formGroupPtr.dereference() ?: return@Pointer null
      val location = locationPtr.dereference() ?: return@Pointer null
      Angular2FormGroupGetCallArrayLiteralScope(formGroup, location)
    }
  }

  private fun findFormSymbol(): PolySymbolScope? =
    resolveFormSymbolForGetCallArrayLiteral(formGroup, location)
    ?: Angular2UnknownFormGroup

  override fun equals(other: Any?): Boolean =
    other === this || (other is Angular2FormGroupGetCallArrayLiteralScope && other.formGroup == formGroup && other.location == location)

  override fun hashCode(): Int =
    31 * formGroup.hashCode() + location.hashCode()

  override fun getModificationCount(): Long = 0


  companion object {
    private val formGroupGetPathRefSymbol = ReferencingPolySymbol.create(
      JS_STRING_LITERALS, "Angular Form control, array or group name", PolySymbolOrigin.empty(),
      *NG_FORM_ANY_CONTROL_PROPS.toTypedArray()
    )
  }

}

fun resolveFormSymbolForGetCallArrayLiteral(root: Angular2FormGroup, literal: JSExpression): Angular2FormAbstractControl? {
  val path = buildPath(literal) ?: return null
  var result: Angular2FormAbstractControl = root
  for (name in path) {
    if (result !is Angular2FormGroup) return null
    result = result.members.find { it.name == name }
             ?: return null
  }
  return result
}

private fun buildPath(literal: JSExpression): List<String>? =
  literal.parent
    ?.asSafely<JSArrayLiteralExpression>()
    ?.expressions
    ?.takeWhile { it != literal }
    ?.map { (it as? JSLiteralExpression)?.takeIf { it.isQuotedLiteral }?.stringValue }
    ?.takeIf { it.none { it == null } }
    ?.let {
      @Suppress("UNCHECKED_CAST")
      it as List<String>
    }