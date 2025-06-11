// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.scopes

import com.intellij.html.polySymbols.PolySymbolsHtmlQueryConfigurator
import com.intellij.html.polySymbols.elements.PolySymbolElementDescriptor
import com.intellij.javascript.polySymbols.jsType
import com.intellij.javascript.polySymbols.types.TypeScriptSymbolTypeSupport
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.query.PolySymbolsQueryExecutorFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDelegate
import com.intellij.polySymbols.utils.PolySymbolsScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ThreeState
import com.intellij.util.asSafely
import com.intellij.util.containers.mapSmartSet
import org.angular2.Angular2Framework
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_ONE_TIME_BINDINGS
import java.util.concurrent.ConcurrentHashMap

internal class OneTimeBindingsScope(tag: XmlTag) : PolySymbolsScopeWithCache<XmlTag, Unit>(Angular2Framework.ID, tag.project, tag, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == NG_DIRECTIVE_ONE_TIME_BINDINGS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val queryExecutor = PolySymbolsQueryExecutorFactory.create(dataHolder)
    val scope = dataHolder.descriptor?.asSafely<PolySymbolElementDescriptor>()
                  ?.symbol?.let { listOf(it) }
                ?: emptyList()
    val attributeSelectors = queryExecutor
      .listSymbolsQuery(NG_DIRECTIVE_ATTRIBUTE_SELECTORS, expandPatterns = true)
      .additionalScope(scope)
      .run()
      .plus(queryExecutor
              .listSymbolsQuery(HTML_ATTRIBUTES, expandPatterns = false)
              .additionalScope(scope)
              .exclude(PolySymbolModifier.VIRTUAL, PolySymbolModifier.ABSTRACT)
              .run()
              .filterIsInstance<PolySymbolsHtmlQueryConfigurator.StandardHtmlSymbol>()
      )
      .filter { it.attributeValue?.required == false }
      .mapSmartSet { it.name }

    val isStrictTemplates = isStrictTemplates(dataHolder)
    for (input in queryExecutor
      .listSymbolsQuery(NG_DIRECTIVE_INPUTS, expandPatterns = false)
      .additionalScope(scope)
      .run()
    ) {
      if (input.pattern != null) continue
      val isOneTimeBinding = withTypeEvaluationLocation(dataHolder) {
        isOneTimeBindingProperty(input)
      }
      if (isStrictTemplates) {
        consumer(Angular2OneTimeBinding(input, dataHolder, !attributeSelectors.contains(input.name), !isOneTimeBinding))
      }
      else if (isOneTimeBinding) {
        consumer(Angular2OneTimeBinding(input, dataHolder, !attributeSelectors.contains(input.name)))
      }
    }

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun createPointer(): Pointer<out PolySymbolsScopeWithCache<XmlTag, Unit>> {
    val tagPtr = dataHolder.createSmartPointer()
    return Pointer {
      tagPtr.dereference()?.let { OneTimeBindingsScope(it) }
    }
  }

  companion object {

    const val PROP_DELEGATE_PRIORITY = "ng-delegate-priority"

    private val ONE_TIME_BINDING_EXCLUDES = listOf(Angular2AttributeValueProvider.NG_CLASS_ATTR)
    private val STRING_TYPE: JSType = JSStringType.STRING_EMPTY_EXPLICIT_TYPE

    @JvmStatic
    fun isOneTimeBindingProperty(property: PolySymbol): Boolean {
      if (ONE_TIME_BINDING_EXCLUDES.contains(property.name) || NG_DIRECTIVE_INPUTS != property.qualifiedKind) {
        return false
      }
      if ((property as? Angular2DirectiveProperty)?.virtualProperty == true) return true
      val type = property.jsType ?: return true
      val source = (property as? PsiSourcedPolySymbol)?.source ?: return true

      return CachedValuesManager.getCachedValue(source) {
        CachedValueProvider.Result.create(ConcurrentHashMap<PolySymbol, Boolean>(),
                                          PsiModificationTracker.MODIFICATION_COUNT)
      }.getOrPut(property) {
        withTypeEvaluationLocation(source) {
          expandStringLiteralTypes(type).isDirectlyAssignableType(
            STRING_TYPE, JSTypeComparingContextService.createProcessingContextWithCache(source))
        }
      }
    }

    private fun expandStringLiteralTypes(type: JSType): JSType =
      TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type, null)
        .transformTypeHierarchy { toApply -> if (toApply is JSPrimitiveType) STRING_TYPE else toApply }
  }

  private class Angular2OneTimeBinding(
    override val delegate: PolySymbol,
    val typeEvaluationLocation: PsiElement,
    val requiresValue: Boolean,
    val resolveOnly: Boolean = false,
  ) : PolySymbolDelegate<PolySymbol>, PsiSourcedPolySymbol {
    override val source: PsiElement?
      get() = (delegate as? PsiSourcedPolySymbol)?.source

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = NG_DIRECTIVE_ONE_TIME_BINDINGS

    override val priority: PolySymbol.Priority
      get() = PolySymbol.Priority.LOW

    override val properties: Map<String, Any>
      get() = super<PolySymbolDelegate>.properties +
              sequenceOf(
                super<PolySymbolDelegate>.priority?.let { Pair(PROP_DELEGATE_PRIORITY, it) },
                if (resolveOnly) Pair(PolySymbol.PROP_HIDE_FROM_COMPLETION, true) else null
              ).filterNotNull()

    // Even though an input property might be required,
    // we need to do the check through AngularMissingRequiredDirectiveInputBindingInspection
    override val required: Boolean
      get() = false

    override val attributeValue: PolySymbolHtmlAttributeValue? by lazy(LazyThreadSafetyMode.PUBLICATION) {
      withTypeEvaluationLocation(typeEvaluationLocation) {
        if (isStrictTemplates(this.psiContext)) {
          PolySymbolHtmlAttributeValue.create(
            PolySymbolHtmlAttributeValue.Kind.PLAIN,
            PolySymbolHtmlAttributeValue.Type.COMPLEX,
            !resolveOnly && !JSResolveUtil.isAssignableJSType(
              jsType, JSStringLiteralTypeImpl("", false, JSTypeSource.EXPLICITLY_DECLARED), null),
            null,
            TypeScriptSymbolTypeSupport.extractEnumLikeType(jsType)
          )
        }
        else {
          val isBoolean = TypeScriptSymbolTypeSupport.isBoolean(jsType, psiContext)
          when {
            isBoolean != ThreeState.NO -> {
              PolySymbolHtmlAttributeValue.create(
                PolySymbolHtmlAttributeValue.Kind.PLAIN,
                PolySymbolHtmlAttributeValue.Type.COMPLEX, false,
                null,
                JSCompositeTypeFactory.createUnionType(
                  JSTypeSource.EXPLICITLY_DECLARED,
                  if (isBoolean == ThreeState.UNSURE)
                    TypeScriptSymbolTypeSupport.extractEnumLikeType(jsType)
                  else
                    null,
                  JSStringLiteralTypeImpl(name, false, JSTypeSource.EXPLICITLY_DECLARED),
                  JSStringLiteralTypeImpl("true", false, JSTypeSource.EXPLICITLY_DECLARED),
                  JSStringLiteralTypeImpl("false", false, JSTypeSource.EXPLICITLY_DECLARED)
                ))
            }
            !requiresValue -> PolySymbolHtmlAttributeValue.create(required = false)
            else -> null
          }
        }
      }
    }

    override fun createPointer(): Pointer<Angular2OneTimeBinding> {
      val delegatePtr = this.delegate.createPointer()
      val evaluationLocationPtr = this.typeEvaluationLocation.createSmartPointer()
      val requiresValue = this.requiresValue
      val resolveOnly = this.resolveOnly
      return Pointer {
        val delegate = delegatePtr.dereference() ?: return@Pointer null
        val evaluationLocation = evaluationLocationPtr.dereference() ?: return@Pointer null
        Angular2OneTimeBinding(delegate, evaluationLocation, requiresValue, resolveOnly)
      }
    }

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
      || delegate.isEquivalentTo(symbol)

    override fun equals(other: Any?): Boolean =
      other is Angular2OneTimeBinding
      && other.delegate == delegate
      && other.requiresValue == requiresValue

    override fun hashCode(): Int =
      31 * delegate.hashCode() + requiresValue.hashCode()

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<PolySymbolDelegate>.getNavigationTargets(project)

    override val psiContext: PsiElement?
      get() = super<PolySymbolDelegate>.psiContext

  }
}