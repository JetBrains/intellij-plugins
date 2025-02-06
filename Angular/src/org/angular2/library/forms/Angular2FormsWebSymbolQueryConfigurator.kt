package org.angular2.library.forms

import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.*
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.Angular2Framework
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlFile
import org.angular2.library.forms.scopes.Angular2FormGroupGetCallArrayLiteralScope
import org.angular2.library.forms.scopes.Angular2FormGroupGetCallLiteralScope
import org.angular2.library.forms.scopes.Angular2FormSymbolsScopeInAttributeValue

class Angular2FormsWebSymbolQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(
    project: Project,
    location: PsiElement?,
    context: WebSymbolsContext,
    allowResolve: Boolean,
  ): List<WebSymbolsScope> {
    if (context.framework == Angular2Framework.ID && location != null) {
      val file = location.containingFile
      if (file is Angular2HtmlFile) {
        if (location is XmlAttribute || location is XmlAttributeValue) {
          val attribute = location.parentOfType<XmlAttribute>(true)
          val name = attribute!!.name
          when (name) {
            FORM_CONTROL_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolsScopeInAttributeValue(attribute),
              SingleSymbolExclusiveScope(ATTRIBUTE_VALUE_TO_FORM_CONTROL_SYMBOL),
            )
            FORM_ARRAY_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolsScopeInAttributeValue(attribute),
              SingleSymbolExclusiveScope(ATTRIBUTE_VALUE_TO_FORM_ARRAY_SYMBOL),
            )
            FORM_GROUP_NAME_ATTRIBUTE -> return listOf(
              Angular2FormSymbolsScopeInAttributeValue(attribute),
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

  private val ATTRIBUTE_VALUE_TO_FORM_CONTROL_SYMBOL = ReferencingWebSymbol.create(
    WebSymbol.HTML_ATTRIBUTE_VALUES, "Angular Form Control Name", WebSymbolOrigin.empty(),
    NG_FORM_CONTROL_PROPS,
  )

  private val ATTRIBUTE_VALUE_TO_FORM_ARRAY_SYMBOL = ReferencingWebSymbol.create(
    WebSymbol.HTML_ATTRIBUTE_VALUES, "Angular Form Array Name", WebSymbolOrigin.empty(),
    NG_FORM_ARRAY_PROPS,
  )

  private val ATTRIBUTE_VALUE_TO_FORM_GROUP_SYMBOL = ReferencingWebSymbol.create(
    WebSymbol.HTML_ATTRIBUTE_VALUES, "Angular Form Group Name", WebSymbolOrigin.empty(),
    NG_FORM_GROUP_PROPS,
  )

  private class SingleSymbolExclusiveScope(private val symbol: WebSymbol) : WebSymbolsScope {

    override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
      if (symbol.qualifiedKind == qualifiedKind)
        listOf(symbol)
      else
        emptyList()

    override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
      symbol.qualifiedKind == qualifiedKind

    override fun createPointer(): Pointer<out WebSymbolsScope> {
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

val NG_FORM_GROUP_FIELDS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-form-group-fields")

val NG_FORM_CONTROL_PROPS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-form-control-props")
val NG_FORM_GROUP_PROPS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-form-group-props")
val NG_FORM_ARRAY_PROPS: WebSymbolQualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, "ng-form-array-props")

val NG_FORM_ANY_CONTROL_PROPS: Set<WebSymbolQualifiedKind> = setOf(
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