// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyIndexedAccessType
import com.intellij.lang.javascript.psi.types.evaluable.JSReferenceType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.asSafely
import com.intellij.util.containers.putValue
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.index.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getForAllKeys
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.lang.html.VueFileType
import java.util.*

class VueComponentsCalculation {
  companion object {
    fun calculateScopeComponents(scope: GlobalSearchScope, globalize: Boolean): ComponentsData {
      val allValues = getForAllKeys(scope, VueComponentsIndex.KEY)
      val libCompResolveMap = mutableMapOf<String, String>()

      val componentData = mutableMapOf<String, MutableList<Pair<PsiElement, Boolean>>>()
      for (value in allValues) {
        val indexData = getVueIndexData(value) ?: continue
        val name = indexData.originalName
        val isGlobal = indexData.isGlobal || globalize
        if (isGlobal && name.endsWith(GLOBAL_BINDING_MARK)) {
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
            if (singleGlobalRegistration != null && singleGlobalRegistration.realName.isNotBlank()) {
              val normalizedName = fromAsset(singleGlobalRegistration.realName)
              val normalizedAlias = fromAsset(
                if (singleGlobalRegistration.alias.isBlank()) name.substringBefore(GLOBAL_BINDING_MARK) else singleGlobalRegistration.alias)
              libCompResolveMap[normalizedAlias] = normalizedName
              componentData.putValue(normalizedName, Pair(singleGlobalRegistration.element, true))
            }
          }
        }
        else {
          componentData.putValue(fromAsset(name), Pair(value, isGlobal))
        }
      }

      val componentsMap = mutableMapOf<String, Pair<PsiElement, Boolean>>()
      for (entry in componentData) {
        componentsMap[entry.key] = selectComponentDefinition(entry.value)
      }
      return ComponentsData(componentsMap, libCompResolveMap)
    }

    private fun findObjectLiteralOfGlobalRegistration(element: JSImplicitElement):
      Pair<JSObjectLiteralExpression, Boolean>? {
      val context = element.context as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val qualifiedReference = indexData?.descriptorQualifiedReference ?: return null

      var resolved: PsiElement? = JSStubBasedPsiTreeUtil.resolveLocally(qualifiedReference, context) ?: return null

      var indexedAccessUsed = indexData.indexedAccessUsed

      resolved = (resolved as? JSVariable)?.jsType?.asSafely<JSApplyIndexedAccessType>()
                   ?.qualifierType?.asSafely<JSReferenceType>()
                   ?.let {
                     indexedAccessUsed = true
                     JSStubBasedPsiTreeUtil.resolveLocally(it.referenceName, resolved!!) ?: return null
                   }
                 ?: resolved
      return objectLiteralFor(resolved)
        ?.let { Pair(it, indexedAccessUsed) }
    }

    private class SingleGlobalRegistration(val realName: String, val alias: String, val element: PsiElement)

    // resolves name of 'singular' registration of Vue.component(ref (SomeComp.name or ref = 'literalName'), ref (SomeComp))
    private fun resolveGlobalComponentName(element: JSImplicitElement,
                                           descriptor: JSObjectLiteralExpression?): SingleGlobalRegistration? {
      val context = element.context as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val nameReference = indexData?.nameQualifiedReference ?: return null

      val nameReferenceParts = nameReference.split('.')
      if (nameReferenceParts.size > 2) return null
      if (nameReferenceParts.size == 2) {
        // allow only Vue.component(SomeComp.name, SomeComp) form
        if (nameReferenceParts[0] != indexData.descriptorQualifiedReference) return null
        // for functional components style, where there is no descriptor - heuristics (vuetify)
        if (descriptor == null) return SingleGlobalRegistration(nameReferenceParts[0], nameReferenceParts[0], context)

        if (!descriptor.isValid) return null
        val property = descriptor.findProperty(nameReferenceParts[1])
        if (property != null) {
          val alias = property.jsType.asSafely<JSStringLiteralTypeImpl>()?.literal ?: ""
          val realName = if ("name" == nameReferenceParts[1]) alias else getNameFromDescriptor(descriptor) ?: alias
          return SingleGlobalRegistration(realName, alias, descriptor)
        }
        return null
      }
      if (descriptor == null) return null
      return (JSStubBasedPsiTreeUtil.resolveLocally(nameReference, context) as? JSVariable)
        ?.jsType.asSafely<JSStringLiteralTypeImpl>()
        ?.literal
        ?.let {
          SingleGlobalRegistration(getNameFromDescriptor(descriptor) ?: "", it, descriptor)
        }
    }

    private fun getNameFromDescriptor(descriptor: JSObjectLiteralExpression): String? =
      (descriptor.findProperty(NAME_PROP)?.jsType as? JSStringLiteralTypeImpl)?.literal

    private fun processComponentGroupRegistration(objLiteral: JSObjectLiteralExpression,
                                                  libCompResolveMap: MutableMap<String, String>,
                                                  componentData: MutableMap<String, MutableList<Pair<PsiElement, Boolean>>>) {
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
            val descriptor = objectLiteralFor(element)

            if (propName != null) {
              val nameFromDescriptor = getTextIfLiteral(descriptor?.findProperty(NAME_PROP)?.value) ?: propName
              // name used in call Vue.component() overrides what was set in descriptor itself
              val normalizedName = fromAsset(propName)
              val realName = fromAsset(nameFromDescriptor)
              libCompResolveMap[normalizedName] = realName
              componentData.putValue(realName, Pair(descriptor ?: element, true))
            }
          }
        }
      }
    }

    private fun selectComponentDefinition(list: List<Pair<PsiElement, Boolean>>): Pair<PsiElement, Boolean> {
      var selected: Pair<PsiElement, Boolean>? = null
      for (componentData in list) {
        val isVue = VueFileType.INSTANCE == componentData.first.containingFile.fileType
        if (componentData.second) {
          if (isVue) return componentData
          selected = componentData
        }
        else if (selected == null && isVue) selected = componentData
      }
      return selected ?: list[0]
    }
  }

  class ComponentsData(val map: Map<String, Pair<PsiElement, Boolean>>,
                       val libCompResolveMap: Map<String, String>)
}
