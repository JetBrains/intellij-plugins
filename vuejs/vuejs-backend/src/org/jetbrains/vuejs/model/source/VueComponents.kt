// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSElementTypesImpl
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.libraries.componentDecorator.isComponentDecorator
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueMixin
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.typed.VueTypedEntitiesProvider

/**
 * Basic resolve from index here (when we have the name literal and the descriptor literal/reference)
 */
object VueComponents {
  fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
    return elements.filter(this::isNotInLibrary)
  }

  fun meaningfulExpression(element: PsiElement?): PsiElement? {
    if (element == null) return null
    return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element)
      .firstOrNull { it !is JSEmbeddedContent }
  }

  fun isNotInLibrary(element: JSImplicitElement): Boolean {
    val file = element.containingFile.viewProvider.virtualFile
    return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
  }

  fun vueMixinFinder(implicitElement: JSImplicitElement): VueMixin? {
    getVueIndexData(implicitElement)?.descriptorQualifiedReference
      ?.takeIf { it.isNotBlank() }
      ?.let { resolveReferenceToVueComponent(implicitElement, it) }
      ?.let { return VueModelManager.getMixin(it) }

    (implicitElement.parent as? JSProperty)
      ?.parent?.asSafely<JSObjectLiteralExpression>()
      ?.let { VueSourceComponent.create(it) }
      ?.let { return VueModelManager.getMixin(it) }

    val call = implicitElement.parent as? JSCallExpression
    if (call != null) {
      return JSStubBasedPsiTreeUtil.findDescendants(call, JSElementTypesImpl.OBJECT_LITERAL_EXPRESSION)
        .firstOrNull { (it.context as? JSArgumentList)?.context == call || (it.context == call) }
        ?.let { VueSourceComponent.create(it) }
        ?.let { VueModelManager.getMixin(it) }
    }
    return null
  }

  fun resolveReferenceToVueComponent(element: PsiElement, reference: String): VueComponent? {
    val context = (element as? JSImplicitElement)?.parent ?: element

    return JSStubBasedPsiTreeUtil.resolveLocally(reference, context)
             ?.let { getVueComponentFromResolve(listOf(it)) }
             ?.let { return it }
           ?: getVueComponentFromResolve(ES6QualifiedNameResolver(context, true).resolveQualifiedName(reference))
  }

  private fun getVueComponentFromResolve(result: Collection<PsiElement>): VueComponent? {
    return result.firstNotNullOfOrNull {
      getComponent(it.applyIf(it is JSImplicitElement) { it.context ?: return@firstNotNullOfOrNull null })
    }
  }

  fun getComponentDecorator(element: JSClass): ES6Decorator? {
    element.attributeList
      ?.decorators
      ?.find(::isComponentDecorator)
      ?.let { return it }
    return (element.context as? ES6ExportDefaultAssignment)
      ?.attributeList
      ?.decorators
      ?.find(::isComponentDecorator)
  }

  fun getComponent(element: PsiElement?): VueComponent? =
    VueTypedEntitiesProvider.getComponent(element)
    ?: getSourceComponent(element)

  fun getSourceComponent(element: PsiElement?): VueSourceComponent<*>? =
    when (val resolved = resolveElementTo(element, JSObjectLiteralExpression::class, JSCallExpression::class,
                                          JSClass::class, JSEmbeddedContent::class, HtmlFileImpl::class)) {
      // {...}
      is JSObjectLiteralExpression -> VueSourceComponent.create(resolved)

      // Vue.extend({...})
      // defineComponent({...})
      is JSCallExpression ->
        if (isComponentDefiningCall(resolved)) {
          resolved.stubSafeCallArguments
            .getOrNull(0)
            ?.let { it as? JSObjectLiteralExpression }
            ?.let { VueSourceComponent.create(it) }
        }
        else null

      // @Component({...}) class MyComponent {...}
      is JSClass ->
        VueSourceComponent.create(resolved)

      // <script setup>
      is JSEmbeddedContent ->
        VueSourceComponent.create(resolved.containingFile)

      // Vue file without script section
      is HtmlFileImpl ->
        if (resolved.virtualFile.isVueFile)
          VueSourceComponent.create(resolved)
        else null

      else -> null
    }

  @StubSafe
  fun getDescriptorFromDecorator(decorator: ES6Decorator): JSObjectLiteralExpression? {
    if (!isComponentDecorator(decorator)) return null

    if (decorator is StubBasedPsiElementBase<*>) {
      decorator.stub?.let {
        return (it.findChildStubByElementType(JSElementTypes.CALL_EXPRESSION) ?: it)
          .findChildStubByElementType(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
          ?.psi as? JSObjectLiteralExpression
      }
    }
    val callExpression = decorator.expression as? JSCallExpression ?: return null
    val arguments = callExpression.arguments
    if (arguments.size == 1) {
      return arguments[0] as? JSObjectLiteralExpression
    }
    return null
  }

  @StubSafe
  fun isComponentDefiningCall(callExpression: JSCallExpression): Boolean =
    getFunctionNameFromVueIndex(callExpression).let {
      it == DEFINE_COMPONENT_FUN || it == DEFINE_NUXT_COMPONENT_FUN || it == EXTEND_FUN || it == DEFINE_OPTIONS_FUN
    }

  @StubSafe
  fun isDefineOptionsCall(callExpression: JSCallExpression): Boolean =
    getFunctionNameFromVueIndex(callExpression) == DEFINE_OPTIONS_FUN

  fun isStrictComponentDefiningCall(callExpression: JSCallExpression): Boolean =
    callExpression.methodExpression?.let {
      JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_COMPONENT_FUN) ||
      JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_NUXT_COMPONENT_FUN) ||
      JSSymbolUtil.isAccurateReferenceExpressionName(it, VUE_NAMESPACE, EXTEND_FUN) ||
      JSSymbolUtil.isAccurateReferenceExpressionName(it, DEFINE_OPTIONS_FUN)
    } ?: false
}

