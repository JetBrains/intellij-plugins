@file:OptIn(IntellijInternalApi::class)

package org.angular2.web.scopes

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.model.Pointer
import com.intellij.openapi.util.IntellijInternalApi
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.RecursionManager
import com.intellij.polySymbols.*
import com.intellij.polySymbols.PolySymbol.Priority
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
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
  : PolySymbolScopeWithCache<Angular2TemplateBindingKey, Unit>(Angular2Framework.ID, binding.project, binding, Unit) {

  @OptIn(IntellijInternalApi::class)
  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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
        consumer(ReferencingPolySymbol.create(NG_TEMPLATE_BINDINGS, "Angular template binding mapping",
                                              Angular2SymbolOrigin.empty,
                                              NG_DIRECTIVE_INPUTS))
      }
      else -> {}
    }
  }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    super.getCodeCompletions(qualifiedName, params, stack).map {
      it.withPriority(Priority.HIGHEST)
    }

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
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

  private class TemplateBindingsSymbol(private val bindings: Angular2TemplateBindings) : PolySymbol {
    override val origin: PolySymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = JS_SYMBOLS

    override val name: @NlsSafe String
      get() = bindings.templateName

    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PROP_JS_TYPE -> property.tryCast(BindingsTypeResolver.get(bindings).resolveTemplateContextType())
        else -> super.get(property)
      }

    override fun createPointer(): Pointer<out PolySymbol> {
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