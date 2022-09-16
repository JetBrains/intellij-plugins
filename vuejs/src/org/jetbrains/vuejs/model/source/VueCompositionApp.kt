// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.model.Pointer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.stubSafeCallArguments
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.index.VueCompositionAppIndex
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponents.Companion.getComponentDescriptor

class VueCompositionApp(override val source: JSCallExpression) : VueDelegatedContainer<VueContainer>(), VueApp {

  override val rootComponent: VueComponent?
    get() = delegate.castSafelyTo<VueComponent>()

  override val components: Map<String, VueComponent>
    get() = (delegate?.components ?: emptyMap()) + getEntitiesAnalysis().components

  override val directives: Map<String, VueDirective>
    get() = (delegate?.directives ?: emptyMap()) + getEntitiesAnalysis().directives

  override val mixins: List<VueMixin>
    get() = (delegate?.mixins ?: emptyList()) + getEntitiesAnalysis().mixins

  override val filters: Map<String, VueFilter>
    get() = (delegate?.filters ?: emptyMap()) + getEntitiesAnalysis().filters

  override val element: String?
    get() = getEntitiesAnalysis().element

  override val delegate: VueContainer?
    get() = getImplicitElement(source)
      ?.let { getParam(it, source, 0) }
      ?.let { initializer ->
        if (initializer is JSObjectLiteralExpression)
          VueModelManager.getApp(initializer)
        else
          CachedValuesManager.getCachedValue(initializer) {
            val container = getComponentDescriptor(initializer)?.let {
              VueModelManager.getComponent(it)
            } as? VueContainer
            CachedValueProvider.Result.create(container, PsiModificationTracker.MODIFICATION_COUNT)
          }
      }

  override fun getProximity(plugin: VuePlugin): VueModelVisitor.Proximity =
    plugin.defaultProximity

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val initializerPtr = source.createSmartPointer()
    return Pointer {
      initializerPtr.dereference()?.let { VueCompositionApp(it) }
    }
  }

  override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueCompositionApp
     && other.source == this.source)

  override fun hashCode(): Int = source.hashCode()

  override fun toString(): String {
    return "VueCompositionApp($source)"
  }

  private fun getEntitiesAnalysis(): EntitiesAnalysis =
    CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(analyzeCall(source), PsiModificationTracker.MODIFICATION_COUNT)
    }

  companion object {

    @StubSafe
    fun getVueElement(call: JSCallExpression): VueScopeElement? {
      val implicitElement = getImplicitElement(call)?.takeIf { isVueContext(it) } ?: return null
      return when (implicitElement.name) {
        COMPONENT_FUN -> {
          val args = getFilteredArgs(call)
          VueModelManager.getComponent(getComponentDescriptor(getParam(implicitElement, call, 1, args))).let {
            val literal = args.getOrNull(0) as? JSLiteralExpression ?: return@let it
            if (it is VueRegularComponent) VueLocallyDefinedRegularComponent(it, literal) else it
          }
        }
        DIRECTIVE_FUN, FILTER_FUN -> getFilteredArgs(call).getOrNull(0)
          ?.castSafelyTo<JSLiteralExpression>()
          ?.let { literal ->
            getTextIfLiteral(literal)?.let { name ->
              if (implicitElement.name == DIRECTIVE_FUN)
                VueSourceDirective(name, literal)
              else
                VueSourceFilter(name, literal)
            }
          }
        MIXIN_FUN -> VueModelManager.getMixin(getComponentDescriptor(getParam(implicitElement, call, 0)) as? VueSourceEntityDescriptor)
        else -> null
      }
    }

    private fun getFilteredArgs(call: JSCallExpression) =
      call.stubSafeCallArguments.dropWhile { it is JSCallExpression }

    private fun getImplicitElement(call: JSCallExpression): JSImplicitElement? =
      call.indexingData
        ?.implicitElements
        ?.find { it.userString == VueCompositionAppIndex.JS_KEY }

    private fun getParam(element: JSImplicitElement,
                         call: JSCallExpression,
                         nr: Int,
                         args: List<PsiElement> = getFilteredArgs(call)): PsiElement? {
      val refName = element.userStringData
      return if (refName != null)
        JSStubBasedPsiTreeUtil.resolveLocally(refName, call, true)
      else args.getOrNull(nr)
    }

    private fun analyzeCall(call: JSCallExpression): EntitiesAnalysis {
      val resolveScope = PsiTreeUtil.findFirstContext(call, false) { JSUtils.isScopeOwner(it) || it is JSFile || it is JSEmbeddedContent }
      val searchScope = GlobalSearchScopeUtil.toGlobalSearchScope(LocalSearchScope(resolveScope ?: call.containingFile), call.project)

      fun <T> processCalls(funName: String, hasName: Boolean, processor: (String, PsiElement, JSLiteralExpression?) -> T?): Sequence<T> =
        resolve(funName, searchScope, VueCompositionAppIndex.KEY)
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


      val components = processCalls(COMPONENT_FUN, true) { name, el, nameLiteral ->
        VueModelManager.getComponent(getComponentDescriptor(el))?.let {
          val component = if (it is VueRegularComponent) VueLocallyDefinedRegularComponent(it, nameLiteral!!) else it
          var delegate: VueScopeElement? = component
          while (true) {
            if (delegate is UserDataHolder) delegate.putUserData(IS_COMPOSITION_APP_COMPONENT_KEY, true)
            if (delegate is VueDelegatedEntitiesContainer<*>) delegate = delegate.delegate
            else break
          }
          Pair(name, component)
        }
      }.toMap()

      val directives = processCalls(DIRECTIVE_FUN, true) { name, el, nameLiteral ->
        Pair(name, VueSourceDirective(name, nameLiteral!!))
      }.toMap()

      val mixins = processCalls(MIXIN_FUN, false) { _, el, _ ->
        VueModelManager.getMixin(getComponentDescriptor(el) as? VueSourceEntityDescriptor)
      }.toList()

      val filters = processCalls(FILTER_FUN, true) { name, el, nameLiteral ->
        Pair(name, VueSourceFilter(name, nameLiteral!!))
      }.toMap()

      val element = resolve(MOUNT_FUN, searchScope, VueCompositionAppIndex.KEY)
        .firstNotNullOfOrNull { element ->
          (element.context as? JSCallExpression)
            ?.let { getFilteredArgs(it) }
            ?.getOrNull(0)
            ?.let { arg -> getTextIfLiteral(arg) }
        }

      return EntitiesAnalysis(components, directives, mixins, filters, element)
    }

    private val IS_COMPOSITION_APP_COMPONENT_KEY = Key.create<Boolean>("vue.composition.app.component")

    fun isCompositionAppComponent(component: VueComponent) =
      component is UserDataHolder && component.getUserData(IS_COMPOSITION_APP_COMPONENT_KEY) == true
  }

  private data class EntitiesAnalysis(val components: Map<String, VueComponent>,
                                      val directives: Map<String, VueDirective>,
                                      val mixins: List<VueMixin>,
                                      val filters: Map<String, VueFilter>,
                                      val element: String?)

}
