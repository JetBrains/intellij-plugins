package org.angular2.library.forms.scopes

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_STRING_LITERALS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.library.forms.Angular2FormAbstractControl
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.impl.Angular2UnknownFormGroup
import java.util.*

class Angular2FormGroupGetCallArrayLiteralScope(private val formGroup: Angular2FormGroup, private val location: JSExpression) : WebSymbolsScope {

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == JS_STRING_LITERALS

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (qualifiedKind == JS_STRING_LITERALS)
      listOf(formGroupGetPathRefSymbol)
    else
      findFormSymbol()?.getSymbols(qualifiedKind, params, scope)
      ?: emptyList()

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsCodeCompletionQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    if (qualifiedName.qualifiedKind == JS_STRING_LITERALS)
      super.getCodeCompletions(qualifiedName, params, scope)
    else
      findFormSymbol()?.getCodeCompletions(qualifiedName, params, scope)
      ?: emptyList()

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsNameMatchQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.qualifiedKind == JS_STRING_LITERALS)
      super.getMatchingSymbols(qualifiedName, params, scope)
    else
      findFormSymbol()?.getMatchingSymbols(qualifiedName, params, scope)
      ?: emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScope> {
    val formGroupPtr = formGroup.createPointer()
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val formGroup = formGroupPtr.dereference() ?: return@Pointer null
      val location = locationPtr.dereference() ?: return@Pointer null
      Angular2FormGroupGetCallArrayLiteralScope(formGroup, location)
    }
  }

  private fun findFormSymbol(): WebSymbol? =
    resolveFormSymbolForGetCallArrayLiteral(formGroup, location)
    ?: Angular2UnknownFormGroup

  override fun equals(other: Any?): Boolean =
    other === this || (other is Angular2FormGroupGetCallArrayLiteralScope && other.formGroup == formGroup && other.location == location)

  override fun hashCode(): Int =
    Objects.hash(formGroup, location)

  override fun getModificationCount(): Long = 0


  companion object {
    private val formGroupGetPathRefSymbol = ReferencingWebSymbol.create(
      JS_STRING_LITERALS, "FormGroup.get() controls", WebSymbolOrigin.empty(),
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