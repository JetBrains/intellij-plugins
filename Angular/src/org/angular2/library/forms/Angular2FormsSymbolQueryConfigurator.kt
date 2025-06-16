package org.angular2.library.forms

import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.*
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.HTML_ATTRIBUTE_VALUES
import com.intellij.polySymbols.js.NAMESPACE_JS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryConfigurator
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.angular2.Angular2Framework
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.library.forms.scopes.Angular2FormGroupGetCallArrayLiteralScope
import org.angular2.library.forms.scopes.Angular2FormGroupGetCallLiteralScope
import org.angular2.library.forms.scopes.Angular2FormSymbolScopeInAttributeValue

class Angular2FormsSymbolQueryConfigurator : PolySymbolQueryConfigurator {

  override fun getScope(
    project: Project,
    location: PsiElement?,
    context: PolyContext,
    allowResolve: Boolean,
  ): List<PolySymbolScope> {
    if (context.framework == Angular2Framework.ID && location != null) {
      val file = location.containingFile ?: return emptyList()
      if (file is Angular2HtmlFile) {
        if (location is XmlAttribute || location is XmlAttributeValue) {
          val attribute = location.parentOfType<XmlAttribute>(true)
          val name = attribute!!.name
          when (name) {
            FORM_CONTROL_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolScopeInAttributeValue(attribute),
              SingleSymbolExclusiveScope(ATTRIBUTE_VALUE_TO_FORM_CONTROL_SYMBOL),
            )
            FORM_ARRAY_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolScopeInAttributeValue(attribute),
              SingleSymbolExclusiveScope(ATTRIBUTE_VALUE_TO_FORM_ARRAY_SYMBOL),
            )
            FORM_GROUP_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolScopeInAttributeValue(attribute),
              SingleSymbolExclusiveScope(ATTRIBUTE_VALUE_TO_FORM_GROUP_SYMBOL),
            )
          }
        }
      }
      else if (file.language.let { it !is Angular2Language && it is JSLanguageDialect && it.optionHolder.isTypeScript }) {
        if (location is JSLiteralExpression && location.isQuotedLiteral || location is JSReferenceExpression && location.qualifier == null) {
          findFormGroupForGetCallParameter(location)
            ?.let { return listOf(Angular2FormGroupGetCallLiteralScope(it)) }
          findFormGroupForGetCallParameterArray(location)
            ?.let { return listOf(Angular2FormGroupGetCallArrayLiteralScope(it, location)) }
        }
      }
    }
    return emptyList()
  }

  private val ATTRIBUTE_VALUE_TO_FORM_CONTROL_SYMBOL = ReferencingPolySymbol.create(
    HTML_ATTRIBUTE_VALUES, "Angular Form control name", PolySymbolOrigin.empty(),
    NG_FORM_CONTROL_PROPS,
  )

  private val ATTRIBUTE_VALUE_TO_FORM_ARRAY_SYMBOL = ReferencingPolySymbol.create(
    HTML_ATTRIBUTE_VALUES, "Angular Form array name", PolySymbolOrigin.empty(),
    NG_FORM_ARRAY_PROPS,
  )

  private val ATTRIBUTE_VALUE_TO_FORM_GROUP_SYMBOL = ReferencingPolySymbol.create(
    HTML_ATTRIBUTE_VALUES, "Angular Form group name", PolySymbolOrigin.empty(),
    NG_FORM_GROUP_PROPS,
  )

  private class SingleSymbolExclusiveScope(private val symbol: PolySymbol) : PolySymbolScope {

    override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: PolySymbolListSymbolsQueryParams, scope: Stack<PolySymbolScope>): List<PolySymbol> =
      if (symbol.qualifiedKind == qualifiedKind)
        listOf(symbol)
      else
        emptyList()

    override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
      symbol.qualifiedKind == qualifiedKind

    override fun createPointer(): Pointer<out PolySymbolScope> {
      val symbolPtr = symbol.createPointer()
      return Pointer {
        symbolPtr.dereference()?.let { SingleSymbolExclusiveScope(it) }
      }
    }

    override fun getModificationCount(): Long = 0

    override fun equals(other: Any?): Boolean =
      other === this || (other is SingleSymbolExclusiveScope && other.symbol == symbol)

    override fun hashCode(): Int =
      symbol.hashCode()
  }
}

fun findFormGroupForGetCallParameter(element: JSExpression): Angular2FormGroup? =
  element
    .parent.asSafely<JSArgumentList>()
    ?.parent?.asSafely<JSCallExpression>()
    ?.methodExpression?.asSafely<JSReferenceExpression>()
    ?.takeIf { it.referenceName == "get" }
    ?.qualifier?.asSafely<JSReferenceExpression>()
    ?.let { Angular2FormsComponent.getFor(it)?.getFormGroupFor(it) }

fun findFormGroupForGetCallParameterArray(element: JSExpression): Angular2FormGroup? =
  element
    .parent.asSafely<JSArrayLiteralExpression>()
    ?.let { findFormGroupForGetCallParameter(it) }

val NG_FORM_GROUP_FIELDS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-form-group-fields"]

val NG_FORM_CONTROL_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-form-control-props"]
val NG_FORM_GROUP_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-form-group-props"]
val NG_FORM_ARRAY_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_JS, "ng-form-array-props"]

val NG_FORM_ANY_CONTROL_PROPS: Set<PolySymbolQualifiedKind> = setOf(
  NG_FORM_CONTROL_PROPS,
  NG_FORM_ARRAY_PROPS,
  NG_FORM_GROUP_PROPS,
)

const val FORM_CONTROL_NAME_ATTRIBUTE: String = "formControlName"
const val FORM_GROUP_NAME_ATTRIBUTE: String = "formGroupName"
const val FORM_ARRAY_NAME_ATTRIBUTE: String = "formArrayName"

val FORM_ANY_CONTROL_NAME_ATTRIBUTES: Set<String> = setOf(FORM_CONTROL_NAME_ATTRIBUTE, FORM_GROUP_NAME_ATTRIBUTE, FORM_ARRAY_NAME_ATTRIBUTE)

const val FORM_GROUP_BINDING: String = "formGroup"

const val FORM_CONTROL_CONSTRUCTOR: String = "FormControl"
const val FORM_GROUP_CONSTRUCTOR: String = "FormGroup"
const val FORM_ARRAY_CONSTRUCTOR: String = "FormArray"

const val FORM_BUILDER_TYPE: String = "FormBuilder"

const val FORM_BUILDER_GROUP_METHOD: String = "group"
const val FORM_BUILDER_ARRAY_METHOD: String = "array"
const val FORM_BUILDER_CONTROL_METHOD: String = "control"