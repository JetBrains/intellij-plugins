// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.attributes.findProperty
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.MIXINS
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.*

abstract class VueSourceContainer(sourceElement: PsiElement,
                                  protected val declaration: PsiElement) : VueContainer {

  override val source: PsiElement? = sourceElement
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val data: List<VueDataProperty> = emptyList()
  override val computed: List<VueComputedProperty> = emptyList()
  override val methods: List<VueMethod> = emptyList()
  override val props: List<VueInputProperty> = emptyList()
  override val emits: List<VueEmitCall> = emptyList()
  override val slots: List<VueSlot> = emptyList()
  override val template: PsiElement? = null
  override val element: String? = null
  override val extends: Any? = null
  override val components: Map<String, VueComponent>
    get () {
      return (declaration as? JSObjectLiteralExpression)?.let { declaration ->
        CachedValuesManager.getCachedValue(declaration) {
          val result = mutableMapOf<String, VueComponent>()

          StreamEx.of(VueComponentOwnDetailsProvider.getLocalComponents(declaration, { _, _ -> true }, true))
            .mapToEntry({ descr -> descr.name }, { descr ->
              descr.element
                ?.let { VueComponents.meaningfulExpression(it) ?: it }
                ?.let { meaningfulElement ->
                  VueComponentsCalculation.getObjectLiteralFromResolve(listOf(meaningfulElement))
                  ?: (meaningfulElement.parent as? ES6ExportDefaultAssignment)
                    ?.let { VueComponents.getExportedDescriptor(it) }
                    ?.let { it.obj ?: it.clazz }
                }
                ?.let { VueModelManager.getComponent(it) }
              ?: VueUnresolvedComponent()
            })
            .into(result)

          CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
      } ?: emptyMap()
    }
  override val directives: Map<String, VueDirective> = emptyMap()
  override val filters: Map<String, VueFilter> = emptyMap()
  override val mixins: List<VueMixin>
    get() {
      return (declaration as? JSObjectLiteralExpression)?.let { declaration ->
        CachedValuesManager.getCachedValue(declaration) {
          CachedValueProvider.Result.create(buildMixinsList(declaration), PsiModificationTracker.MODIFICATION_COUNT)
        }
      } ?: emptyList()
    }

  companion object {
    private fun buildMixinsList(declaration: JSObjectLiteralExpression): List<VueMixin> {
      val mixinsProperty = findProperty(declaration, MIXINS) ?: return emptyList()
      val elements = resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile.originalFile), VueMixinBindingIndex.KEY)
                     ?: return emptyList()
      val original = CompletionUtil.getOriginalOrSelf<PsiElement>(mixinsProperty)
      return StreamEx.of(elements)
        .filter { PsiTreeUtil.isAncestor(original, it.parent, false) }
        .map { VueComponents.vueMixinDescriptorFinder(it) }
        .nonNull()
        .map { VueModelManager.getMixin(it!!) }
        .nonNull()
        .toList()
    }
  }
}
