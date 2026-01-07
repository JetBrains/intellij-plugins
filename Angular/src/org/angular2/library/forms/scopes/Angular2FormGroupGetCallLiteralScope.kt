package org.angular2.library.forms.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createComplexPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createPatternSequence
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createStringMatch
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createSymbolReferencePlaceholder
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.utils.unwrapMatchedSymbols
import com.intellij.util.containers.map2Array
import org.angular2.library.forms.Angular2FormGroup
import org.angular2.library.forms.NG_FORM_ANY_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2SymbolOrigin

class Angular2FormGroupGetCallLiteralScope(private val formGroup: Angular2FormGroup) : PolySymbolScope {

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_STRING_LITERALS

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (kind == JS_STRING_LITERALS)
      listOf(FormGroupGetPathSymbol)
    else
      formGroup.getSymbols(kind, params, stack)

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    if (qualifiedName.kind == JS_STRING_LITERALS)
      super.getCodeCompletions(qualifiedName, params, stack)
        .filter {
          it.name != "." && (!it.name.endsWith(".") || it.symbol?.unwrapMatchedSymbols()?.lastOrNull()?.kind == NG_FORM_GROUP_PROPS)
        }
    else
      formGroup.getCodeCompletions(qualifiedName, params, stack)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.kind == JS_STRING_LITERALS)
      super.getMatchingSymbols(qualifiedName, params, stack)
    else
      formGroup.getMatchingSymbols(qualifiedName, params, stack)

  override fun createPointer(): Pointer<out PolySymbolScope> {
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
    object FormGroupGetPathSymbol : PolySymbolWithPattern, PolySymbolScope, Angular2Symbol {

      override val name: @NlsSafe String
        get() = "FormGroup.get() path"

      override val origin: PolySymbolOrigin
        get() = Angular2SymbolOrigin.empty

      override val kind: PolySymbolKind
        get() = JS_STRING_LITERALS

      override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
        kind == JS_STRING_LITERALS

      override fun getModificationCount(): Long = 0

      override val pattern: PolySymbolPattern
        get() = createComplexPattern(
          ComplexPatternOptions(symbolsResolver = PolySymbolPatternReferenceResolver(
            *NG_FORM_ANY_CONTROL_PROPS.map2Array {
              PolySymbolPatternReferenceResolver.Reference(kind = it)
            }
          )),
          false,
          createPatternSequence(
            createSymbolReferencePlaceholder(),
            createComplexPattern(
              ComplexPatternOptions(
                symbolsResolver = PolySymbolPatternReferenceResolver(
                  *NG_FORM_ANY_CONTROL_PROPS.map2Array {
                    PolySymbolPatternReferenceResolver.Reference(kind = it)
                  }
                ),
                repeats = true,
                isRequired = false,
              ),
              false,
              createPatternSequence(
                createStringMatch("."),
                PolySymbolPatternFactory.createCompletionAutoPopup(false),
                createSymbolReferencePlaceholder(),
              )
            )
          )
        )

      override fun createPointer(): Pointer<out FormGroupGetPathSymbol> =
        Pointer.hardPointer(this)

    }
  }

}