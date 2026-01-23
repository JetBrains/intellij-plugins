// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.asSafely
import com.intellij.util.containers.putValue
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.index.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.index.VUE_COMPONENTS_INDEX_KEY
import org.jetbrains.vuejs.index.getForAllKeys
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueNamedComponent
import java.util.*

class VueComponentsCalculation {
  companion object {
    fun calculateScopeComponents(scope: GlobalSearchScope, globalize: Boolean): ComponentsData {
      val allValues = getForAllKeys(scope, VUE_COMPONENTS_INDEX_KEY)
      val libCompResolveMap = mutableMapOf<PsiElement, VueNamedComponent>()

      val componentData = mutableMapOf<String, MutableList<Pair<VueNamedComponent, Boolean>>>()
      for (value in allValues) {
        val indexData = getVueIndexData(value) ?: continue
        val isGlobal = indexData.isGlobal || globalize
        if (isGlobal && indexData.originalName.endsWith(GLOBAL_BINDING_MARK)) {
          // we come here when in Vue.component() first argument is a reference,
          // i.e. we do not know the name of global component at the indexing moment
          // two cases are possible: single registration Vue.component(SomeComp.name, SomeComp)
          // or iteration-registration, when components references are gathered into some object as properties,
          // and further iterated calling Vue.component()
          // this is especially hard to distinguish them for cases like VueResolveTest.testResolveWithExplicitForInComponentsBinding
          // ! we are using information whether we met indexed property access to understand of it was collection of objects
          val pair = findObjectLiteralOfGlobalRegistration(value)
          if (true == pair?.second) {
            // indexed access -> treat as collection of components
            processComponentGroupRegistration(pair.first, libCompResolveMap, componentData)
          }
          else {
            // treat as single registration
            val singleGlobalRegistration = resolveGlobalComponentName(value, pair?.first)
            if (singleGlobalRegistration != null) {
              val normalizedName = fromAsset(singleGlobalRegistration.component.name)
              //val normalizedAlias = fromAsset(singleGlobalRegistration.alias.ifBlank { name.substringBefore(GLOBAL_BINDING_MARK) })
              libCompResolveMap[singleGlobalRegistration.alias ?: value] = singleGlobalRegistration.component
              componentData.putValue(normalizedName, Pair(singleGlobalRegistration.component, true))
            }
          }
        }
        else {
          VueSourceComponent.create(value)
            ?.asSafely<VueNamedComponent>()
            ?.let { componentData.putValue(fromAsset(it.name), Pair(it, isGlobal)) }
        }
      }

      return ComponentsData(componentData.values.map { selectComponentDefinition(it) }, libCompResolveMap)
    }

    private fun findObjectLiteralOfGlobalRegistration(element: JSImplicitElement):
      Pair<JSObjectLiteralExpression, Boolean>? {
      val context = element.context as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val qualifiedReference = indexData?.descriptorQualifiedReference ?: return null

      val resolved = JSStubBasedPsiTreeUtil.resolveLocally(qualifiedReference, context) ?: return null

      val indexedAccessUsed = indexData.indexedAccessUsed

      return objectLiteralFor(resolved)
        ?.let { Pair(it, indexedAccessUsed) }
    }

    private class SingleGlobalRegistration(val component: VueNamedComponent, val alias: PsiElement?)

    // resolves name of 'singular' registration of Vue.component(ref (SomeComp.name or ref = 'literalName'), ref (SomeComp))
    private fun resolveGlobalComponentName(
      element: JSImplicitElement,
      descriptor: JSObjectLiteralExpression?,
    ): SingleGlobalRegistration? {
      val context = element.context as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val nameReference = indexData?.nameQualifiedReference ?: return null

      val nameReferenceParts = nameReference.split('.')
      if (nameReferenceParts.size > 2) return null
      if (nameReferenceParts.size == 2) {
        // allow only Vue.component(SomeComp.name, SomeComp) form
        if (nameReferenceParts[0] != indexData.descriptorQualifiedReference) return null
        // for functional components style, where there is no descriptor - heuristics (vuetify)
        if (descriptor == null)
          return null
        // TODO - analyze how to provide this component
        // SingleGlobalRegistration(nameReferenceParts[0], nameReferenceParts[0], context)

        if (!descriptor.isValid) return null
        val property = descriptor.findProperty(nameReferenceParts[1])
        if (property != null) {
          val component = VueSourceComponent.create(descriptor) ?: return null
          val alias = property.takeIf { nameReferenceParts[1] != NAME_PROP }
          return SingleGlobalRegistration(
            component as? VueNamedComponent
            ?: VueLocallyDefinedComponent.create(component, alias ?: return null)
            ?: return null, alias?.takeIf { component is VueNamedComponent })
        }
        return null
      }
      val component = VueSourceComponent.create(descriptor ?: return null) ?: return null
      return (JSStubBasedPsiTreeUtil.resolveLocally(nameReference, context) as? JSVariable)
        ?.let {
          SingleGlobalRegistration(
            component as? VueNamedComponent
            ?: VueLocallyDefinedComponent.create(component, it)
            ?: return null, it.takeIf { component is VueNamedComponent })
        }
    }

    private fun processComponentGroupRegistration(
      objLiteral: JSObjectLiteralExpression,
      libCompResolveMap: MutableMap<PsiElement, VueNamedComponent>,
      componentData: MutableMap<String, MutableList<Pair<VueNamedComponent, Boolean>>>,
    ) {
      // object properties iteration
      val queue = ArrayDeque<PsiElement>()
      queue.addAll(objLiteral.propertiesIncludingSpreads)
      val visited = mutableSetOf<PsiElement>()
      while (!queue.isEmpty()) {
        val element = queue.removeFirst()
        // technically, I can write spread to itself or a ring
        if (!visited.add(element)) continue

        when (element) {
          is JSSpreadExpression -> {
            objectLiteralFor(element.expression)
              ?.let { queue.addAll(it.propertiesIncludingSpreads) }
          }
          is JSProperty -> {
            val propName = element.name
            if (propName != null) {
              val component =
                VueComponents.getComponent(element)
                  ?.let { it as? VueNamedComponent ?: VueLocallyDefinedComponent.create(it, element) }
                ?: continue
              // name used in call Vue.component() overrides what was set in descriptor itself
              libCompResolveMap[element] = component
              componentData.putValue(fromAsset(component.name), Pair(component, true))
            }
          }
        }
      }
    }

    private fun selectComponentDefinition(list: List<Pair<VueNamedComponent, Boolean>>): Pair<VueNamedComponent, Boolean> {
      var selected: Pair<VueNamedComponent, Boolean>? = null
      for (componentData in list) {
        val isVue = componentData.first.psiContext is VueFile
        if (componentData.second) {
          if (isVue) return componentData
          selected = componentData
        }
        else if (selected == null && isVue) selected = componentData
      }
      return selected ?: list[0]
    }
  }

  class ComponentsData(
    val list: List<Pair<VueNamedComponent, Boolean>>,
    val libCompResolveMap: Map<PsiElement, VueNamedComponent>,
  )
}
