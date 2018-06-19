// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.putValue
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.index.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getForAllKeys
import org.jetbrains.vuejs.index.getVueIndexData
import java.util.*

/**
 * @author Irina.Chernushina on 1/12/2018.
 */
class VueComponentsCalculation {
  companion object {
    fun calculateScopeComponents(scope: GlobalSearchScope): VueComponentsCache.ComponentsData {
      val allValues = getForAllKeys(scope, VueComponentsIndex.KEY)
      val libCompResolveMap = mutableMapOf<String, String>()

      val componentData = mutableMapOf<String, MutableList<Pair<PsiElement, Boolean>>>()
      for (value in allValues) {
        val indexData = getVueIndexData(value)
        val name = indexData.originalName
        val isGlobal = indexData.isGlobal
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
              libCompResolveMap.put(normalizedAlias, normalizedName)
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
        componentsMap.put(entry.key, selectComponentDefinition(entry.value))
      }
      return VueComponentsCache.ComponentsData(componentsMap, libCompResolveMap)
    }

    private fun findObjectLiteralOfGlobalRegistration(element: JSImplicitElement):
      Pair<JSObjectLiteralExpression, Boolean>? {
      element.parent as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val reference = indexData.descriptorRef ?: return null

      val scope = createLocalResolveScope(element)
      var resolved: PsiElement? = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope) ?: return null
      resolved = (resolved as? JSVariable)?.initializerOrStub ?: resolved
      var indexedAccessUsed = indexData.groupRegistration
      if (resolved is JSIndexedPropertyAccessExpression) {
        indexedAccessUsed = true
        resolved = (resolved as? JSIndexedPropertyAccessExpression)?.qualifier
      }
      if (resolved == null) return null

      if (resolved is JSReferenceExpression) {
        val variants = resolved.multiResolve(false)
        val literal = getObjectLiteralFromResolve(variants.mapNotNull { if (it.isValidResult) it.element else null }.toList())
        if (literal != null) return Pair(literal, indexedAccessUsed)
      }
      resolved = VueComponents.literalFor(resolved)

      val obj = resolved ?: return null
      return Pair(obj, indexedAccessUsed)
    }

    private class SingleGlobalRegistration(val realName: String, val alias: String, val element: PsiElement)

    // resolves name of 'singular' registration of Vue.component(ref (SomeComp.name or ref = 'literalName'), ref (SomeComp))
    private fun resolveGlobalComponentName(element: JSImplicitElement,
                                           descriptor: JSObjectLiteralExpression?): SingleGlobalRegistration? {
      element.parent as? JSCallExpression ?: return null
      val indexData = getVueIndexData(element)
      val reference = indexData.nameRef ?: return null
      val scope = createLocalResolveScope(element)

      val parts = reference.split('.')
      if (parts.size > 2) return null
      if (parts.size == 2) {
        // allow only Vue.component(SomeComp.name, SomeComp) form
        if (parts[0] != indexData.descriptorRef) return null
        // for functional components style, where there is no descriptor - heuristics (vuetify)
        if (descriptor == null) return SingleGlobalRegistration(parts[0], parts[0], element.parent)

        if (!descriptor.isValid) return null
        val property = descriptor.findProperty(parts[1])
        if (property != null) {
          val alias = (property.value as? JSLiteralExpression)?.stringValue ?: ""
          val realName = if ("name" == parts[1]) alias else propStrVal(descriptor, "name") ?: alias
          return SingleGlobalRegistration(realName, alias, descriptor)
        }
        return null
      }
      if (descriptor == null) return null
      var resolved = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope)
      if (resolved is JSVariable) resolved = resolved.initializerOrStub
      val strLiteral = resolved as? JSLiteralExpression
      if (strLiteral != null && strLiteral.isQuotedLiteral) {
        return SingleGlobalRegistration(propStrVal(descriptor, "name") ?: "", strLiteral.stringValue ?: "", descriptor)
      }
      return null
    }

    private fun propStrVal(descriptor: JSObjectLiteralExpression, name: String) : String? =
      (descriptor.findProperty(name)?.value as? JSLiteralExpression)?.stringValue

    private fun createLocalResolveScope(element: PsiElement): PsiElement =
      PsiTreeUtil.getContextOfType(element, JSCatchBlock::class.java, JSClass::class.java, JSExecutionScope::class.java)
      ?: element.containingFile

    private fun getObjectLiteralFromResolve(result: Collection<PsiElement>): JSObjectLiteralExpression? {
      return result.mapNotNull(fun(it: PsiElement): JSObjectLiteralExpression? {
        val element: PsiElement? = (it as? JSVariable)?.initializerOrStub ?: it
        return VueComponents.literalFor(element)
      }).firstOrNull()
    }

    private fun processComponentGroupRegistration(objLiteral: JSObjectLiteralExpression,
                                                  libCompResolveMap: MutableMap<String, String>,
                                                  componentData: MutableMap<String, MutableList<Pair<PsiElement, Boolean>>>) {
      // object properties iteration
      val queue = ArrayDeque<PsiElement>()
      queue.addAll(objLiteral.children)
      val visited = mutableSetOf<PsiElement>()
      while (!queue.isEmpty()) {
        val element = queue.removeFirst()
        // technically, I can write spread to itself or a ring
        if (visited.contains(element)) continue
        visited.add(element)

        val asSpread = element as? JSSpreadExpression
        if (asSpread != null) {
          val spreadExpression = asSpread.expression
          if (spreadExpression is JSReferenceExpression) {
            val literal = getObjectLiteralFromResolve(resolveToValid(spreadExpression))
            if (literal != null) queue.addAll(literal.children)
          }
          else if (spreadExpression is JSObjectLiteralExpression) {
            queue.addAll(spreadExpression.children)
          }
          continue
        }
        val asProperty = element as? JSProperty
        if (asProperty != null) {
          val propName = asProperty.name
          if (propName != null && asProperty.value != null) {
            val candidate = asProperty.value!!
            var descriptor = VueComponents.literalFor(candidate)
            if (descriptor == null && candidate is JSReferenceExpression) {
              descriptor = getObjectLiteralFromResolve(resolveToValid(candidate))
            }
            val nameFromDescriptor = getTextIfLiteral(descriptor?.findProperty("name")?.value) ?: propName
            // name used in call Vue.component() overrides what was set in descriptor itself
            val normalizedName = fromAsset(propName)
            val realName = fromAsset(nameFromDescriptor)
            libCompResolveMap.put(normalizedName, realName)
            componentData.putValue(realName, Pair(descriptor ?: asProperty, true))
          }
        }
      }
    }

    private fun resolveToValid(reference: JSReferenceExpression) =
      reference.multiResolve(false).mapNotNull { if (it.isValidResult) it.element else null }.toList()

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
}