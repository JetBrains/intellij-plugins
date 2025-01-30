package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Priority
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.Angular2Framework
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_TEMPLATE_BINDINGS

class TemplateBindingKeyScope(binding: Angular2TemplateBindingKey)
  : WebSymbolsScopeWithCache<Angular2TemplateBindingKey, Unit>(Angular2Framework.ID, binding.project, binding, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val templateBindings = dataHolder.parentOfType<Angular2TemplateBindings>() ?: return
    val templateName = templateBindings.templateName
    getDirectiveInputsFor(templateBindings)
      .filter { it.name != templateName }
      .forEach(consumer)
    consumer(ReferencingWebSymbol.create(NG_TEMPLATE_BINDINGS, "Template binding mapping for ${templateBindings.templateName}",
                                         Angular2SymbolOrigin.empty,
                                         NG_DIRECTIVE_INPUTS))
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
    qualifiedKind == NG_TEMPLATE_BINDINGS || qualifiedKind == NG_DIRECTIVE_INPUTS

  override fun createPointer(): Pointer<TemplateBindingKeyScope> {
    val bindingPtr = dataHolder.createSmartPointer()
    return Pointer {
      bindingPtr.dereference()?.let { TemplateBindingKeyScope(it) }
    }
  }

  override fun getModificationCount(): Long = 0

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
        .filter { it.name == templateName || (it.name.startsWith(templateName) && it.name.getOrNull(templateName.length)?.isUpperCase() == true) }
    }

  }

}