package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbol.Priority
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.query.WebSymbolNameConversionRules
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.Angular2Framework
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_TEMPLATE_BINDINGS

class TemplateBindingKeyScope(binding: Angular2TemplateBindingKey) : WebSymbolsScopeWithCache<Angular2TemplateBindingKey, Unit>(Angular2Framework.ID, binding.project, binding, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val templateBindings = dataHolder.parentOfType<Angular2TemplateBindings>()
    val directives = templateBindings
      ?.enclosingAttribute
      ?.descriptor
      ?.asSafely<Angular2AttributeDescriptor>()
      ?.sourceDirectives
    if (directives == null) return
    val templateName = templateBindings.templateName
    directives.asSequence()
      .flatMap { it.inputs }
      .filter { it.name.startsWith(templateName) && it.name.getOrNull(templateName.length)?.isUpperCase() == true }
      .forEach(consumer)
    consumer(ReferencingWebSymbol.create(NG_TEMPLATE_BINDINGS, "Template binding mapping for $templateName", Angular2SymbolOrigin.empty,
                                         NG_DIRECTIVE_INPUTS))
  }

  override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsCodeCompletionQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    super.getCodeCompletions(qualifiedName, params, scope).map {
      it.withPriority(Priority.HIGHEST)
    }

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == NG_TEMPLATE_BINDINGS || qualifiedKind == NG_DIRECTIVE_INPUTS

  override fun createPointer(): Pointer<TemplateBindingKeyScope> {
    val bindingPtr = dataHolder.createSmartPointer()
    return Pointer {
      bindingPtr.dereference()?.let { TemplateBindingKeyScope(it) }
    }
  }

  override fun getModificationCount(): Long = 0

  private class TemplateBindingMappingSymbol(
    private val templateName: String,
  ) : WebSymbol {
    override val origin: WebSymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: @NlsSafe SymbolNamespace
      get() = NAMESPACE_JS

    override val kind: @NlsSafe SymbolKind
      get() = NG_TEMPLATE_BINDINGS.kind

    override val name: @NlsSafe String
      get() = "Template binding mapping for $templateName"

    override val pattern: WebSymbolsPattern =
      WebSymbolsPatternFactory.createComplexPattern(
        ComplexPatternOptions(
          priority = priority,
          symbolsResolver = WebSymbolsPatternReferenceResolver(
            WebSymbolsPatternReferenceResolver.Reference(qualifiedKind = NG_DIRECTIVE_INPUTS, nameConversionRules = listOf(
              WebSymbolNameConversionRules.builder()
                .addMatchNamesRule(NG_DIRECTIVE_INPUTS) {
                  listOf(templateName + StringUtil.capitalize(it))
                }
                .addCanonicalNamesRule(NG_DIRECTIVE_INPUTS) {
                  listOf(it)
                }
                .addCompletionVariantsRule(NG_DIRECTIVE_INPUTS) {
                  listOf(StringUtil.decapitalize(it.removePrefix(templateName)))
                }
                .build()
            ))
          )), false,
        WebSymbolsPatternFactory.createPatternSequence(
          WebSymbolsPatternFactory.createSymbolReferencePlaceholder(),
        )
      )

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)

  }

}