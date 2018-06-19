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

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.ES6QualifiedNameResolver
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.index.getVueIndexData

/**
 * @author Irina.Chernushina on 9/26/2017.
 *
 * Basic resolve from index here (when we have the name literal and the descriptor literal/reference)
 */
class VueComponents {
  companion object {
    fun onlyLocal(elements: Collection<JSImplicitElement>): List<JSImplicitElement> {
      return elements.filter(this::isNotInLibrary)
    }
    
    fun literalFor(element: PsiElement?): JSObjectLiteralExpression? {
      if (element is JSObjectLiteralExpression) return element
      if (element  == null) return null
      return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element)
        .mapNotNull { it as? JSObjectLiteralExpression }
        .firstOrNull()
    }
    
    fun meaningfulExpression(element: PsiElement?): PsiElement? {
      if (element == null) return element
      
      return JSStubBasedPsiTreeUtil.calculateMeaningfulElements(element)
        .firstOrNull { it !is JSEmbeddedContent }
    }

    fun isNotInLibrary(element : JSImplicitElement): Boolean {
      val file = element.containingFile.viewProvider.virtualFile
      return !JSProjectUtil.isInLibrary(file, element.project) && !JSLibraryUtil.isProbableLibraryFile(file)
    }

    fun findComponentDescriptor(element: JSImplicitElement): JSObjectLiteralExpression? {
      val parent = element.parent
      if (parent is JSCallExpression) {
        val reference = getVueIndexData(element).descriptorRef ?: return null
        return resolveReferenceToVueComponent(element, reference)?.obj
      }
      return (parent as? JSProperty)?.context as? JSObjectLiteralExpression
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

    private fun resolveReferenceToVueComponent(element: PsiElement, reference: String): VueComponentDescriptor? {
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
      return result.mapNotNull(fun(it: PsiElement): VueComponentDescriptor? {
        val element: PsiElement? = (it as? JSVariable)?.initializerOrStub ?: it
        if (element is JSObjectLiteralExpression) return VueComponentDescriptor(obj = element)
        if (element is JSClassExpression<*>) {
          val parentExport = element.parent as? ES6ExportDefaultAssignment ?: return null
          return getExportedDescriptor(parentExport)
        }
        val objLiteral = VueComponents.literalFor(element!!) ?: return null
        return VueComponentDescriptor(obj = objLiteral)
      }).firstOrNull()
    }

    fun isComponentDecorator(decorator: ES6Decorator): Boolean {
      val callExpression = decorator.expression as? JSCallExpression
      if (callExpression != null) {
        return "Component" == (callExpression.methodExpression as? JSReferenceExpression)?.referenceName
      } else {
        val reference = decorator.expression as? JSReferenceExpression
        return "Component" == reference?.referenceName
      }
    }

    fun getElementComponentDecorator(element: PsiElement): ES6Decorator? {
      val attrList = PsiTreeUtil.getChildOfType(element, JSAttributeList::class.java) ?: return null
      val decorator = PsiTreeUtil.getChildOfType(attrList, ES6Decorator::class.java) ?: return null
      if (!isComponentDecorator(decorator)) return null
      return decorator
    }

    fun getExportedDescriptor(defaultExport: JSExportAssignment): VueComponentDescriptor? {
      val exportedObjectLiteral = defaultExport.stubSafeElement as? JSObjectLiteralExpression
      if (exportedObjectLiteral != null) return VueComponentDescriptor(obj = exportedObjectLiteral)
      val attrList = PsiTreeUtil.getChildOfType(defaultExport, JSAttributeList::class.java) ?: return null
      val decorator = PsiTreeUtil.getChildOfType(attrList, ES6Decorator::class.java) ?: return null
      val objectDescriptor = VueComponents.getDescriptorFromDecorator(decorator)
      val classDescriptor = defaultExport.stubSafeElement as? JSClassExpression<*>
      if (objectDescriptor == null && classDescriptor == null) return null
      return VueComponentDescriptor(objectDescriptor, classDescriptor)
    }

    fun getDescriptorFromDecorator(decorator: ES6Decorator): JSObjectLiteralExpression? {
      val callExpression = decorator.expression as? JSCallExpression ?: return null
      if ("Component" != (callExpression.methodExpression as? JSReferenceExpression)?.referenceName) return null

      val arguments = callExpression.arguments
      if (arguments.size == 1) {
        return arguments[0] as? JSObjectLiteralExpression
      }
      return null
    }
  }
}

class VueComponentDescriptor(val obj: JSObjectLiteralExpression? = null,
                             val clazz: JSClassExpression<*>? = null) {
  init {
    assert(obj != null || clazz != null)
  }
}