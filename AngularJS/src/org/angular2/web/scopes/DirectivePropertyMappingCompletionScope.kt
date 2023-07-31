// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.javascript.webSymbols.symbols.JSPropertySymbol
import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.siblings
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2SourceDirective
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS
import org.angular2.web.references.Angular2DirectivePropertyLiteralReferencesProvider

/**
 * Due to complicated nature of the Angular mapping syntax,
 * this scope is handling only code completion of Angular directive properties.
 * Reference resolution is being provided separately by [Angular2DirectivePropertyLiteralReferencesProvider]
 */
class DirectivePropertyMappingCompletionScope(element: JSElement)
  : WebSymbolsScopeWithCache<JSElement, Unit>(Angular2Framework.ID, element.project, element, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    dataHolder
      .takeIf { it is JSReferenceExpression || it is JSLiteralExpression }
      ?.let { Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective(it, false) }
      ?.let { (kind, directive, hostDirective, jsProperty) ->
        consumer(inputOutputReference)

        val context = dataHolder

        val otherPropertyMappings = Angular2SourceDirective.readDirectivePropertyMappings(jsProperty)
          .filter { entry ->
            entry.value.declaringElement != context
            && (context !is JSLiteralExpression || context.stringValue?.replace(CompletionUtil.DUMMY_IDENTIFIER, "") != entry.key)
            && (context !is JSReferenceExpression || entry.value.declaringElement != context.siblings().firstOrNull { it is JSLiteralExpression })
          }

        val filterAndConsume = { symbol: WebSymbol ->
          if (!otherPropertyMappings.containsKey(symbol.name))
            consumer(symbol)
        }

        if (hostDirective) {
          if (kind == Angular2DecoratorUtil.INPUTS_PROP)
            directive.bindings.inputs.forEach(filterAndConsume)
          else
            directive.bindings.outputs.forEach(filterAndConsume)
        }
        else {
          val symbolKind = if (kind == Angular2DecoratorUtil.INPUTS_PROP)
            KIND_NG_DIRECTIVE_INPUTS
          else
            KIND_NG_DIRECTIVE_OUTPUTS
          directive.typeScriptClass
            ?.asWebSymbol()
            ?.getJSPropertySymbols(null)
            ?.filter { property ->
              val sources = Angular2SourceDirective.getPropertySources(property.source)
              sources.isNotEmpty()
              && sources.none { source ->
                source.attributeList?.decorators?.any { dec -> dec.decoratorName == INPUT_DEC || dec.decoratorName == OUTPUT_DEC } == true
              }
            }
            ?.map { Angular2FieldPropertySymbol(it, symbolKind, directive.sourceElement.project, directive.typeScriptClass) }
            ?.forEach(filterAndConsume)
        }
      }
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    /* Do not support reference resolution */
    emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<JSElement, Unit>> {
    val elementPtr = dataHolder.createSmartPointer()
    return Pointer {
      elementPtr.dereference()?.let { DirectivePropertyMappingCompletionScope(it) }
    }
  }

  override fun isExclusiveFor(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    namespace == NAMESPACE_JS && kind == KIND_JS_STRING_LITERALS
    && dataHolder
      .takeIf { it is JSReferenceExpression || it is JSLiteralExpression }
      ?.let { jsElement ->
        CachedValuesManager.getCachedValue(jsElement) {
          CachedValueProvider.Result.create(
            Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective(jsElement, false) != null,
            PsiModificationTracker.MODIFICATION_COUNT)
        }
      } == true


  override fun provides(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    namespace == NAMESPACE_JS && (
      kind == KIND_JS_STRING_LITERALS
      || kind == KIND_NG_DIRECTIVE_INPUTS
      || kind == KIND_NG_DIRECTIVE_OUTPUTS)

  private object AngularEmptyOrigin : WebSymbolOrigin {
    override val framework: FrameworkId =
      Angular2Framework.ID
  }

  private val inputOutputReference = ReferencingWebSymbol(
    NAMESPACE_JS,
    KIND_JS_STRING_LITERALS,
    "Directive property",
    AngularEmptyOrigin,
    WebSymbolQualifiedKind(NAMESPACE_JS, KIND_NG_DIRECTIVE_INPUTS),
    WebSymbolQualifiedKind(NAMESPACE_JS, KIND_NG_DIRECTIVE_OUTPUTS),
    priority = WebSymbol.Priority.HIGHEST
  )

  private class Angular2FieldPropertySymbol(
    delegate: JSPropertySymbol,
    override val kind: SymbolKind,
    override val project: Project,
    val owner: TypeScriptClass?,
  ) : WebSymbolDelegate<JSPropertySymbol>(delegate), Angular2Symbol {

    override val namespace: SymbolNamespace
      get() = NAMESPACE_JS

    override val origin: WebSymbolOrigin
      get() = super<Angular2Symbol>.origin

    override val type: Any?
      get() = if (kind == KIND_NG_DIRECTIVE_OUTPUTS) {
        Angular2TypeUtils.extractEventVariableType(super<WebSymbolDelegate>.type as? JSType)
      }
      else {
        Angular2EntityUtils.jsTypeFromAcceptInputType(owner, name) ?: super<WebSymbolDelegate>.type as? JSType
      }

    override fun createPointer(): Pointer<out Angular2FieldPropertySymbol> {
      val delegatePtr = delegate.createPointer()
      val kind = kind
      val project = project
      val ownerPtr = owner?.createSmartPointer()
      return Pointer {
        val owner = ownerPtr?.let { it.dereference() ?: return@Pointer null }
        val delegate = delegatePtr.dereference() ?: return@Pointer null
        Angular2FieldPropertySymbol(delegate, kind, project, owner)
      }
    }

  }

}