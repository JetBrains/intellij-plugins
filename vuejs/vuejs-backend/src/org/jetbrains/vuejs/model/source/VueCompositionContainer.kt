// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.openapi.project.DumbService
import com.intellij.polySymbols.PolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VUE_COMPOSITION_APP_INDEX_JS_KEY
import org.jetbrains.vuejs.index.VUE_COMPOSITION_APP_INDEX_KEY
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueDelegatedContainer
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueMixin
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.model.VueScopeElement
import org.jetbrains.vuejs.model.source.VueComponents.getComponent
import org.jetbrains.vuejs.web.unwrapVueSymbolWithProximity

abstract class VueCompositionContainer(
  private val mode: VueMode,
) : VueDelegatedContainer<VueContainer>() {

  abstract override val source: JSElement

  val plugins: List<VuePlugin>
    get() = getEntitiesAnalysis().plugins

  protected open fun pluginChain(): List<VuePlugin> =
    emptyList()

  override val components: Map<String, VueNamedComponent>
    get() = buildMap {
      val container = delegate?.takeUnless { it is VueComponent }
      if (container != null) {
        putAll(container.components)
      }

      for (plugin in pluginChain()) {
        putAll(plugin.components)
      }

      putAll(getEntitiesAnalysis().components)
    }

  override val directives: Map<String, VueDirective>
    get() = buildMap {
      val container = delegate
      if (container != null) {
        putAll(container.directives)
      }

      for (plugin in pluginChain()) {
        putAll(plugin.directives)
      }

      putAll(getEntitiesAnalysis().directives)
    }

  override val mixins: List<VueMixin>
    get() = (delegate?.mixins ?: emptyList()) + getEntitiesAnalysis().mixins

  override val filters: Map<String, VueFilter>
    get() = (delegate?.filters ?: emptyMap()) + getEntitiesAnalysis().filters

  override val provides: List<VueProvide>
    get() = (delegate?.provides ?: emptyList()) + getEntitiesAnalysis().provides

  override val element: String?
    get() = getEntitiesAnalysis().element

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()

  private fun getEntitiesAnalysis(): EntitiesAnalysis =
    CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        analyzeCall(source, mode),
        DumbService.getInstance(source.project).modificationTracker,
        PsiModificationTracker.MODIFICATION_COUNT
      )
    }

  companion object {

    @StubSafe
    fun getVueElement(call: JSCallExpression): VueScopeElement? {
      val implicitElement = getImplicitElement(call)
                              ?.takeIf { isVueContext(it) }
                            ?: return null

      return when (implicitElement.name) {
        COMPONENT_FUN -> {
          val args = getFilteredArgs(call)
          getParam(implicitElement, call, 1, args)
            ?.let { getComponent(it) }
            ?.let {
              val literal = args.getOrNull(0) as? JSLiteralExpression
                            ?: return@let it
              VueLocallyDefinedComponent.create(it, literal)
            }
        }

        DIRECTIVE_FUN,
        FILTER_FUN,
          -> getFilteredArgs(call)
          .getOrNull(0)
          ?.asSafely<JSLiteralExpression>()
          ?.let { literal ->
            getTextIfLiteral(literal)?.let { name ->
              if (implicitElement.name == DIRECTIVE_FUN)
                VueSourceDirective(name, literal)
              else
                VueSourceFilter(name, literal)
            }
          }

        MIXIN_FUN -> VueModelManager.getMixin(getComponent(getParam(implicitElement, call, 0)))

        else -> null
      }
    }

    private fun getFilteredArgs(call: JSCallExpression) =
      call.stubSafeCallArguments.dropWhile { it is JSCallExpression }

    @JvmStatic
    protected fun getImplicitElement(call: JSCallExpression): JSImplicitElement? =
      call.indexingData
        ?.implicitElements
        ?.find { it.userString == VUE_COMPOSITION_APP_INDEX_JS_KEY }

    @JvmStatic
    protected fun getParam(
      element: JSImplicitElement,
      call: JSCallExpression,
      nr: Int,
      args: List<PsiElement> = getFilteredArgs(call),
    ): PsiElement? {
      val refName = element.userStringData
      return if (refName != null)
        JSStubBasedPsiTreeUtil.resolveLocally(refName, call, true)
      else args.getOrNull(nr)
    }

    private fun analyzeCall(
      call: JSElement,
      mode: VueMode,
    ): EntitiesAnalysis {
      val resolveScope = PsiTreeUtil.findFirstContext(call, false) { JSUtils.isScopeOwner(it) || it is JSFile || it is JSEmbeddedContent }
      val searchScope = GlobalSearchScopeUtil.toGlobalSearchScope(LocalSearchScope(resolveScope ?: call.containingFile), call.project)

      fun <T> processCalls(funName: String, hasName: Boolean, processor: (String, PsiElement, JSLiteralExpression?) -> T?): Sequence<T> =
        resolve(funName, searchScope, VUE_COMPOSITION_APP_INDEX_KEY)
          .asSequence()
          .filter { resolveScope == null || PsiTreeUtil.isContextAncestor(resolveScope, it, false) }
          .mapNotNull { el ->
            val defineCall = el.context as? JSCallExpression ?: return@mapNotNull null
            val args = getFilteredArgs(defineCall)
            val nameLiteral = if (hasName)
              args.getOrNull(0) as? JSLiteralExpression ?: return@mapNotNull null
            else null
            val name = if (hasName)
              getTextIfLiteral(nameLiteral) ?: return@mapNotNull null
            else ""
            getParam(el, defineCall, if (hasName) 1 else 0, args)
              ?.let { processor(name, it, nameLiteral) }
          }

      val plugins = processCalls(USE_FUN, false) { _, el, _ ->
        VueCompositionPlugin.get(el, mode)
      }.toList()

      val components = processCalls(COMPONENT_FUN, true) { name, el, nameLiteral ->
        getComponent(el)
          ?.let { VueLocallyDefinedComponent.create(it, nameLiteral ?: return@let null, isCompositionAppComponent = true) }
          ?.let { component -> Pair(name, component) }
      }.toMap()

      val directives = processCalls(DIRECTIVE_FUN, true) { name, el, nameLiteral ->
        Pair(name, VueSourceDirective(name, nameLiteral!!, el, mode))
      }.toMap()

      val mixins = processCalls(MIXIN_FUN, false) { _, el, _ ->
        VueModelManager.getMixin(getComponent(el))
      }.toList()

      val filters = processCalls(FILTER_FUN, true) { name, _, nameLiteral ->
        Pair(name, VueSourceFilter(name, nameLiteral!!))
      }.toMap()

      val provides = resolve(PROVIDE_FUN, searchScope, VUE_COMPOSITION_APP_INDEX_KEY)
        .asSequence()
        .filter { resolveScope == null || PsiTreeUtil.isContextAncestor(resolveScope, it, false) }
        .mapNotNull { el ->
          val provideCall = el.context as? JSCallExpression ?: return@mapNotNull null
          val referenceName = el.userStringData
          val literalKey = provideCall.stubSafeCallArguments.getOrNull(0).asSafely<JSLiteralExpression>()
          when {
            referenceName != null -> JSStubBasedPsiTreeUtil.resolveLocally(referenceName, provideCall).asSafely<PsiNamedElement>()
              ?.let { VueSourceProvide(referenceName, provideCall, it) }
            literalKey != null -> getTextIfLiteral(literalKey)?.let { VueSourceProvide(it, literalKey) }
            else -> null
          }
        }.toList()

      val element = resolve(MOUNT_FUN, searchScope, VUE_COMPOSITION_APP_INDEX_KEY)
        .firstNotNullOfOrNull { element ->
          (element.context as? JSCallExpression)
            ?.let { getFilteredArgs(it) }
            ?.getOrNull(0)
            ?.let { arg -> getTextIfLiteral(arg) }
        }

      return EntitiesAnalysis(
        plugins = plugins,
        components = components,
        directives = directives,
        mixins = mixins,
        filters = filters,
        element = element,
        provides = provides,
      )
    }

    fun isCompositionAppComponent(component: VueComponent): Boolean =
      (component as? PolySymbol)?.unwrapVueSymbolWithProximity().let {
        it is VueLocallyDefinedComponent<*> && it.isCompositionAppComponent
      }
  }

  private data class EntitiesAnalysis(
    val plugins: List<VuePlugin>,
    val components: Map<String, VueNamedComponent>,
    val directives: Map<String, VueDirective>,
    val mixins: List<VueMixin>,
    val filters: Map<String, VueFilter>,
    val element: String?,
    val provides: List<VueProvide>,
  )

}
