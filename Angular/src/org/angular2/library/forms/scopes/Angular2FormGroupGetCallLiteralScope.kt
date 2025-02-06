package org.angular2.library.forms.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.containers.Stack
import com.intellij.util.containers.map2Array
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createComplexPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createPatternSequence
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createStringMatch
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createSymbolReferencePlaceholder
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.qualifiedKind
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS
import org.angular2.web.Angular2SymbolOrigin

class Angular2FormGroupGetCallLiteralScope(private val formGroup: Angular2FormGroup) : WebSymbolsScope {

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == JS_STRING_LITERALS

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (qualifiedKind == JS_STRING_LITERALS)
      listOf(FormGroupGetPathSymbol)
    else
      formGroup.getSymbols(qualifiedKind, params, scope)

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsCodeCompletionQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    if (qualifiedName.qualifiedKind == JS_STRING_LITERALS)
      super.getCodeCompletions(qualifiedName, params, scope)
        .filter { it.name != "." && (!it.name.endsWith(".") || it.symbol?.unwrapMatchedSymbols()?.lastOrNull()?.qualifiedKind == NG_FORM_GROUP_PROPS) }
    else
      formGroup.getCodeCompletions(qualifiedName, params, scope)

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsNameMatchQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.qualifiedKind == JS_STRING_LITERALS)
      super.getMatchingSymbols(qualifiedName, params, scope)
    else
      formGroup.getMatchingSymbols(qualifiedName, params, scope)

  override fun createPointer(): Pointer<out WebSymbolsScope> {
    val formGroupPtr = formGroup.createPointer()
    return Pointer {
      val formGroup = formGroupPtr.dereference() ?: return@Pointer null
      Angular2FormGroupGetCallLiteralScope(formGroup)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this || (other is Angular2FormGroupGetCallLiteralScope && other.formGroup == formGroup)

  override fun hashCode(): Int =
    formGroup.hashCode()

  override fun getModificationCount(): Long = 0

  companion object {
    object FormGroupGetPathSymbol : WebSymbol {

      override val name: @NlsSafe String
        get() = "FormGroup.get() path"

      override val origin: WebSymbolOrigin
        get() = Angular2SymbolOrigin.empty

      override val namespace: @NlsSafe SymbolNamespace
        get() = NAMESPACE_JS

      override val kind: @NlsSafe SymbolKind
        get() = KIND_JS_STRING_LITERALS

      override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
        qualifiedKind == JS_STRING_LITERALS

      override val pattern: WebSymbolsPattern?
        get() = createComplexPattern(
          ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
            *NG_FORM_ANY_CONTROL_PROPS.map2Array {
              WebSymbolsPatternReferenceResolver.Reference(qualifiedKind = it)
            }
          )),
          false,
          createPatternSequence(
            createSymbolReferencePlaceholder(),
            createComplexPattern(
              ComplexPatternOptions(
                symbolsResolver = WebSymbolsPatternReferenceResolver(
                  *NG_FORM_ANY_CONTROL_PROPS.map2Array {
                    WebSymbolsPatternReferenceResolver.Reference(qualifiedKind = it)
                  }
                ),
                repeats = true,
                isRequired = false,
              ),
              false,
              createPatternSequence(
                createStringMatch("."),
                WebSymbolsPatternFactory.createCompletionAutoPopup(false),
                createSymbolReferencePlaceholder(),
              )
            )
          )
        )

      override fun createPointer(): Pointer<out WebSymbol> =
        Pointer.hardPointer(this)

    }
  }

}