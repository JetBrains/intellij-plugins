// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.index.getVueIndexData

/**
 * Basic resolve from index here (when we have the name literal and the descriptor literal/reference)
 */
class VueComponents {
  companion object {
    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter(this::isNotInLibrary)
    }

    fun meaningfulExpression(element: PsiElement?): PsiElement? {
      if (element == null) return element
      return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element)
        .firstOrNull { it !is JSEmbeddedContent }
    }

    fun isNotInLibrary(element: JSImplicitElement): Boolean {
      val file = element.containingFile.viewProvider.virtualFile
      return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
    }

    fun vueMixinDescriptorFinder(implicitElement: JSImplicitElement): JSObjectLiteralExpression? {
      val typeString = getVueIndexData(implicitElement).descriptorRef
      if (!StringUtil.isEmptyOrSpaces(typeString)) {
        val expression = resolveReferenceToVueComponent(implicitElement, typeString!!)
        if (expression?.obj != null) {
          return expression.obj
        }
      }
      val mixinObj = (implicitElement.parent as? JSProperty)?.parent as? JSObjectLiteralExpression
      if (mixinObj != null) return mixinObj

      val call = implicitElement.parent as? JSCallExpression
      if (call != null) {
        return JSStubBasedPsiTreeUtil.findDescendants(call, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
          .firstOrNull { (it.context as? JSArgumentList)?.context == call || (it.context == call) }
      }
      return null
    }

    fun resolveReferenceToVueComponent(element: PsiElement, reference: String): VueComponentDescriptor? {
      val scope = createLocalResolveScope(element)

      val resolvedLocally = JSStubBasedPsiTreeUtil.resolveLocally(reference, scope)
      if (resolvedLocally != null) {
        val literalFromResolve = getVueComponentFromResolve(listOf(resolvedLocally))
        if (literalFromResolve != null) {
          return literalFromResolve
        }
      }

      val elements = ES6QualifiedNameResolver(scope).resolveQualifiedName(reference)
      return getVueComponentFromResolve(elements)
    }

    private fun createLocalResolveScope(element: PsiElement): PsiElement =
      PsiTreeUtil.getContextOfType(element, JSCatchBlock::class.java, JSClass::class.java, JSExecutionScope::class.java)
      ?: element.containingFile

    private fun getVueComponentFromResolve(result: Collection<PsiElement>): VueComponentDescriptor? {
      return result.mapNotNull(::getComponentDescriptor).firstOrNull()
    }

    fun isComponentDecorator(decorator: ES6Decorator): Boolean {
      return decorator.decoratorName == "Component"
    }

    fun getComponentDecorator(element: JSClass): ES6Decorator? {
      element.attributeList
        ?.decorators
        ?.find(this::isComponentDecorator)
        ?.let { return it }
      return (element.context as? ES6ExportDefaultAssignment)
        ?.attributeList
        ?.decorators
        ?.find(this::isComponentDecorator)
    }

    fun getComponentDescriptor(element: PsiElement?): VueComponentDescriptor? {
      when (val resolved = resolveElementTo(element, JSObjectLiteralExpression::class, JSCallExpression::class, JSClass::class)) {
        // {...}
        is JSObjectLiteralExpression -> return VueComponentDescriptor(resolved)

        // Vue.extend({...})
        // defineComponent({...})
        is JSCallExpression ->
          if (isExtendVueCall(resolved) || isDefineComponentCall(resolved)) {
            PsiTreeUtil.getStubChildOfType(resolved.argumentList!!, JSObjectLiteralExpression::class.java)
              ?.let { return VueComponentDescriptor(it) }
          }

        // @Component({...}) class MyComponent {...}
        is JSClassExpression ->
          return VueComponentDescriptor(getComponentDecorator(resolved)?.let { getDescriptorFromDecorator(it) },
                                        resolved)
      }
      return null
    }

    @StubSafe
    fun getDescriptorFromDecorator(decorator: ES6Decorator): JSObjectLiteralExpression? {
      if (!isComponentDecorator(decorator)) return null

      if (decorator is StubBasedPsiElementBase<*>) {
        decorator.stub?.let {
          return it.findChildStubByType(JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
            ?.psi
        }
      }
      val callExpression = decorator.expression as? JSCallExpression ?: return null
      val arguments = callExpression.arguments
      if (arguments.size == 1) {
        return arguments[0] as? JSObjectLiteralExpression
      }
      return null
    }

    @StubUnsafe
    private fun isExtendVueCall(callExpression: JSCallExpression): Boolean {
      return JSSymbolUtil.isAccurateReferenceExpressionName(
        callExpression.methodExpression as? JSReferenceExpression, VUE_NAMESPACE, EXTEND_FUN)
    }

    @StubUnsafe
    private fun isDefineComponentCall(callExpression: JSCallExpression): Boolean {
      return callExpression.methodExpression
        ?.castSafelyTo<JSReferenceExpression>()
        ?.takeIf { it.qualifier == null && it.referenceName == DEFINE_COMPONENT_FUN } != null
    }
  }
}

class VueComponentDescriptor(val obj: JSObjectLiteralExpression? = null,
                             val clazz: JSClass? = null) {
  init {
    assert(obj != null || clazz != null)
  }
}
