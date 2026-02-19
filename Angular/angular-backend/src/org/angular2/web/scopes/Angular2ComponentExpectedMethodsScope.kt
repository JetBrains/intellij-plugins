package org.angular2.web.scopes

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.JSFunctionTypeImpl
import com.intellij.lang.javascript.psi.types.JSNamedType
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSParameterTypeDecoratorImpl
import com.intellij.lang.javascript.psi.types.JSTypeBaseImpl
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.js.JS_EXPECTED_METHODS
import com.intellij.polySymbols.js.JS_PARAMETERS
import com.intellij.polySymbols.js.jsType
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.PolySymbolDelegate
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.web.NG_COMPONENT_LIFECYCLE_HOOKS

internal class Angular2ComponentExpectedMethodsScope(cls: TypeScriptClass) :
  PolySymbolScopeWithCache<TypeScriptClass, Unit>(cls.project, cls, Unit) {
  override fun initialize(
    consumer: (PolySymbol) -> Unit,
    cacheDependencies: MutableSet<Any>,
  ) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    PolySymbolQueryExecutorFactory.create(dataHolder)
      .listSymbolsQuery(NG_COMPONENT_LIFECYCLE_HOOKS) {}
      .forEach {
        consumer(NgComponentLifecycleHookWrapper(it, dataHolder))
      }
  }

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == JS_EXPECTED_METHODS

  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<TypeScriptClass, Unit>> {
    val clsPtr = dataHolder.createSmartPointer()
    return Pointer {
      clsPtr.dereference()?.let { Angular2ComponentExpectedMethodsScope(it) }
    }
  }

  private data class NgComponentLifecycleHookWrapper(
    override val delegate: PolySymbol,
    override val psiContext: TypeScriptClass,
  ) : PolySymbolDelegate<PolySymbol> {

    override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
      PolySymbolDocumentationTarget.create(
        this, location,
        PolySymbolDocumentationProvider<NgComponentLifecycleHookWrapper> { symbol, location ->
          (symbol.delegate
             .getDocumentationTarget(location)
             .asSafely<PolySymbolDocumentationTarget>()
             ?.documentation
           ?: PolySymbolDocumentation.create(symbol, location) {})
            .withDefinition(
              "lifecycle hook".withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, symbol.psiContext, false) +
              " " + symbol.name.withColor(Angular2HighlightingUtils.TextAttributesKind.TS_INSTANCE_METHOD,
                                          symbol.psiContext,
                                          false) +
              "(): " + "void".withColor(Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD, symbol.psiContext, false)
            )
        })

    override val kind: PolySymbolKind
      get() = JS_EXPECTED_METHODS

    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PROP_JS_TYPE -> JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(psiContext) {
          val typeSource = JSTypeSourceFactory.createTypeSource(psiContext)
          property.tryCast(JSFunctionTypeImpl(typeSource, buildParameters(), JSNamedTypeFactory.createVoidType(typeSource)))
        }
        else -> super[property]
      }

    private fun buildParameters(): List<JSParameterTypeDecorator> {
      if (delegate !is PolySymbolScope) return emptyList()

      val typeSource = JSTypeSourceFactory.createTypeSource(psiContext)
      return PolySymbolQueryExecutorFactory.create(psiContext)
        .listSymbolsQuery(JS_PARAMETERS, false) {
          additionalScope(delegate)
        }
        .map { parameter ->
          JSParameterTypeDecoratorImpl(
            parameter.name,
            (parameter.jsType as? JSTypeBaseImpl)
              ?.withNewSource(typeSource)
              ?.substitute(psiContext)
              ?.transformTypeHierarchy {
                if (it is JSNamedType && it.qualifiedName.name == "__CLASS_NAME__")
                  psiContext.jsType
                else it
              }, false, false, true)
        }
    }

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      symbol == this ||
      delegate.isEquivalentTo(symbol) ||
      (symbol is NgComponentLifecycleHookWrapper
       && delegate.isEquivalentTo(symbol.delegate))

    override fun createPointer(): Pointer<out PolySymbolDelegate<PolySymbol>> {
      val delegatePtr = delegate.createPointer()
      val psiContextPtr = psiContext.createSmartPointer()
      return Pointer {
        val delegate = delegatePtr.dereference() ?: return@Pointer null
        val psiContext = psiContextPtr.dereference() ?: return@Pointer null
        NgComponentLifecycleHookWrapper(delegate, psiContext)
      }
    }

  }
}
