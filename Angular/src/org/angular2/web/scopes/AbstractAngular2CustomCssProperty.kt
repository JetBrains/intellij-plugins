package org.angular2.web.scopes

import com.intellij.css.frontback.icons.CssFrontbackApiIcons
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.CssNamedItemPresentation
import com.intellij.psi.css.resolve.CssCustomPropertyReference
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.CSS_PROPERTIES
import com.intellij.webSymbols.documentation.WebSymbolDocumentation
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.web.Angular2SymbolOrigin
import org.jetbrains.annotations.Nls

abstract class AbstractAngular2CustomCssProperty<T : PsiElement>(
  final override val sourceElement: T,
  info: Angular2AttributeNameParser.PropertyBindingInfo,
) : WebSymbolDeclaredInPsi, PsiSourcedWebSymbol {

  protected abstract fun getInitialOffset(): Int

  override val psiContext: PsiElement?
    get() = sourceElement

  override val source: PsiElement? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    CssCustomPropertyReference.multiResolve(sourceElement, name).firstNotNullOfOrNull { it.element }
  }

  final override val textRangeInSourceElement: TextRange? =
    getInitialOffset().let { TextRange(info.nameOffset + it, info.nameOffset + it + info.name.length) }

  final override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin.Companion.empty

  final override val namespace: @NlsSafe SymbolNamespace
    get() = CSS_PROPERTIES.namespace

  final override val kind: @NlsSafe SymbolKind
    get() = CSS_PROPERTIES.kind

  final override val name: @NlsSafe String = info.name

  final override fun validator(): RenameValidator =
    CustomCssPropertyNameValidator

  abstract val valueText: String?

  final override val descriptionSections: Map<@Nls String, @Nls String>
    get() = valueText
              ?.withColor(Angular2Language, sourceElement)
              ?.let { mapOf("Value" to it) }
            ?: emptyMap()

  final override fun createDocumentation(location: PsiElement?): WebSymbolDocumentation? =
    WebSymbolDocumentation.Companion.create(
      this,
      location,
      definition = "css property ".withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, sourceElement,
                                             wrapWithCodeTag = false) +
                   name.withColor(Angular2HighlightingUtils.TextAttributesKind.CSS_PROPERTY, sourceElement, wrapWithCodeTag = false)
    )

  @Suppress("HardCodedStringLiteral")
  override val presentation: TargetPresentation =
    TargetPresentation.builder(name.removePrefix("--"))
      .icon(CssFrontbackApiIcons.Custom_property)
      .containerText(CssNamedItemPresentation.getLocationString(sourceElement))
      .presentation()

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<WebSymbolDeclaredInPsi>.getNavigationTargets(project) +
    CssCustomPropertyReference.multiResolve(sourceElement, name)
      .mapNotNull { it.element?.let { SymbolNavigationService.getInstance().psiElementNavigationTarget(it) } }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<PsiSourcedWebSymbol>.isEquivalentTo(symbol)
    || (symbol is WebSymbol && symbol.qualifiedKind == CSS_PROPERTIES && symbol.name == name)

  abstract override fun createPointer(): Pointer<out AbstractAngular2CustomCssProperty<T>>

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is AbstractAngular2CustomCssProperty<*>
    && other.javaClass == javaClass
    && other.sourceElement == sourceElement

  override fun hashCode(): Int =
    sourceElement.hashCode()

  private object CustomCssPropertyNameValidator : RenameValidator {
    override fun validate(newName: String): RenameValidationResult =
      when {
        !newName.startsWith("--") ->
          RenameValidationResult.Companion.invalid(
            Angular2Bundle.Companion.message("angular.symbol.css-custom-property.error.rename.must-start-with-two-dashes"))
        newName.length <= 2 ->
          RenameValidationResult.Companion.invalid(
            Angular2Bundle.Companion.message("angular.symbol.css-custom-property.error.rename.must-not-be-empty"))
        else ->
          RenameValidationResult.Companion.ok()
      }
  }
}