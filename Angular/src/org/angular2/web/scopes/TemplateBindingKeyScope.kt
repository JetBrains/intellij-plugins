package org.angular2.web.scopes

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_SYMBOLS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.WebSymbol.Priority
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.Angular2Framework
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.isTemplateBindingDirectiveInput
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_TEMPLATE_BINDINGS

class TemplateBindingKeyScope(binding: Angular2TemplateBindingKey)
  : WebSymbolsScopeWithCache<Angular2TemplateBindingKey, Unit>(Angular2Framework.ID, binding.project, binding, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val templateBindings = dataHolder.parentOfType<Angular2TemplateBindings>() ?: return
    when ((dataHolder.parent as? Angular2TemplateBinding ?: return).keyKind) {
      Angular2TemplateBinding.KeyKind.LET -> {
        TemplateBindingsSymbol(CompletionUtil.getOriginalOrSelf(templateBindings))
          .getJSPropertySymbols()
          .forEach { consumer(it) }
      }
      Angular2TemplateBinding.KeyKind.BINDING -> {
        val templateName = templateBindings.templateName
        RecursionManager.runInNewContext {
          getDirectiveInputsFor(templateBindings)
            .filter { it.name != templateName }
            .forEach(consumer)
        }
        consumer(ReferencingWebSymbol.create(NG_TEMPLATE_BINDINGS, "Template binding mapping for ${templateBindings.templateName}",
                                             Angular2SymbolOrigin.empty,
                                             NG_DIRECTIVE_INPUTS))
      }
      else -> {}
    }
  }

  override fun getCodeCompletions(
    qualifiedName: WebSymbolQualifiedName,
    params: WebSymbolsCodeCompletionQueryParams,
    scope: Stack<WebSymbolsScope>,
  ): List<WebSymbolCodeCompletionItem> =
    super.getCodeCompletions(qualifiedName, params, scope).map {
      it.withPriority(Priority.HIGHEST)
    }

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == NG_TEMPLATE_BINDINGS
    || qualifiedKind == NG_DIRECTIVE_INPUTS
    || qualifiedKind == JS_PROPERTIES

  override fun createPointer(): Pointer<TemplateBindingKeyScope> {
    val bindingPtr = dataHolder.createSmartPointer()
    return Pointer {
      bindingPtr.dereference()?.let { TemplateBindingKeyScope(it) }
    }
  }

  override fun getModificationCount(): Long = 0

  private class TemplateBindingsSymbol(private val bindings: Angular2TemplateBindings) : WebSymbol {
    override val origin: WebSymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: @NlsSafe SymbolNamespace
      get() = NAMESPACE_JS

    override val kind: @NlsSafe SymbolKind
      get() = KIND_JS_SYMBOLS

    override val name: @NlsSafe String
      get() = bindings.templateName

    override val type: Any?
      get() = BindingsTypeResolver.get(bindings)
        .resolveTemplateContextType()

    override fun createPointer(): Pointer<out WebSymbol> {
      val bindingsPtr = bindings.createSmartPointer()
      return Pointer {
        bindingsPtr.dereference()?.let { TemplateBindingsSymbol(it) }
      }
    }
  }

  companion object {
    fun getDirectiveInputsFor(templateBindings: Angular2TemplateBindings): List<Angular2DirectiveProperty> {
      val directives = templateBindings
        .enclosingAttribute
        ?.descriptor
        ?.asSafely<Angular2AttributeDescriptor>()
        ?.sourceDirectives
      if (directives == null) return emptyList()
      val templateName = templateBindings.templateName
      return directives
        .flatMap { it.inputs }
        .filter { it.name == templateName || isTemplateBindingDirectiveInput(it.name, templateName) }
    }

  }

}