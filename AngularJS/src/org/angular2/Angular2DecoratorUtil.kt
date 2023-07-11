// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.injections.JSInjectionUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.util.PsiTreeUtil.getContextOfType
import com.intellij.psi.util.PsiTreeUtil.getStubChildrenOfTypeAsList
import com.intellij.util.ArrayUtil.contains
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.index.Angular2IndexingHandler
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angularjs.index.AngularJSIndexingHandler

object Angular2DecoratorUtil {
  const val DIRECTIVE_DEC = "Directive"
  const val COMPONENT_DEC = "Component"
  const val PIPE_DEC = "Pipe"
  const val MODULE_DEC = "NgModule"
  const val INPUT_DEC = "Input"
  const val OUTPUT_DEC = "Output"
  const val ATTRIBUTE_DEC = "Attribute"
  const val VIEW_CHILD_DEC = "ViewChild"
  const val VIEW_CHILDREN_DEC = "ViewChildren"
  const val VIEW_DEC = "View"
  const val NAME_PROP = "name"
  const val SELECTOR_PROP = "selector"
  const val EXPORT_AS_PROP = "exportAs"
  const val INPUTS_PROP = "inputs"
  const val OUTPUTS_PROP = "outputs"
  const val STANDALONE_PROP = "standalone"
  const val IMPORTS_PROP = "imports"
  const val EXPORTS_PROP = "exports"
  const val DECLARATIONS_PROP = "declarations"
  const val ENTRY_COMPONENTS_PROP = "entryComponents"
  const val HOST_DIRECTIVES_PROP = "hostDirectives"
  const val BOOTSTRAP_PROP = "bootstrap"
  const val TEMPLATE_URL_PROP = "templateUrl"
  const val TEMPLATE_PROP = "template"
  const val STYLE_URLS_PROP = "styleUrls"
  const val STYLES_PROP = "styles"
  const val REQUIRED_PROP = "required"
  const val ALIAS_PROP = "alias"
  const val DIRECTIVE_PROP = "directive"

  @JvmStatic
  fun isLiteralInNgDecorator(element: PsiElement?, propertyName: String, vararg decoratorNames: String): Boolean {
    val parent = (element as? JSLiteralExpression)?.takeIf { it.isQuotedLiteral }?.parent?.asSafely<JSProperty>()
    return parent != null
           && propertyName == parent.name
           && (getContextOfType(parent, ES6Decorator::class.java)
      ?.let { decorator -> isAngularEntityDecorator(decorator, *decoratorNames) } == true)
  }

  @JvmStatic
  fun findDecorator(attributeListOwner: JSAttributeListOwner, name: String): ES6Decorator? {
    return findDecorator(attributeListOwner, *arrayOf(name))
  }

  @StubSafe
  @JvmStatic
  fun findDecorator(attributeListOwner: JSAttributeListOwner, vararg names: String): ES6Decorator? {
    val list = attributeListOwner.attributeList
    if (list == null || names.isEmpty()) {
      return null
    }
    for (decorator in getStubChildrenOfTypeAsList(list, ES6Decorator::class.java)) {
      if (isAngularEntityDecorator(decorator, *names)) {
        return decorator
      }
    }
    if (attributeListOwner is TypeScriptClassExpression) {
      val context = attributeListOwner.getContext() as? JSAttributeListOwner
      if (context != null) {
        return findDecorator(context, *names)
      }
    }
    return null
  }

  @JvmStatic
  fun isPrivateMember(element: JSPsiElementBase): Boolean {
    if (element is JSAttributeListOwner) {
      val attributeListOwner = element as JSAttributeListOwner
      return attributeListOwner.attributeList != null
             && attributeListOwner.attributeList!!.accessType == JSAttributeList.AccessType.PRIVATE
    }
    return false
  }

  /**
   * Returns null for all literals other than string, supports string concatenation.
   */
  @StubUnsafe
  @JvmStatic
  fun getPropertyStringValue(decorator: ES6Decorator?, name: String): String? {
    return getExpressionStringValue(getProperty(decorator, name)?.value)
  }

  @StubUnsafe
  @JvmStatic
  fun getExpressionStringValue(value: JSExpression?): String? {
    return when {
      value is JSBinaryExpression -> {
        JSInjectionUtil.getConcatenationText(value)
      }
      value is JSLiteralExpression && value.isQuotedLiteral -> {
        AngularJSIndexingHandler.unquote(value)
      }
      else -> null
    }
  }

  @StubSafe
  @JvmStatic
  fun getObjectLiteralInitializer(decorator: ES6Decorator?): JSObjectLiteralExpression? {
    for (child in getStubChildrenOfTypeAsList(decorator, PsiElement::class.java)) {
      if (child is JSCallExpression) {
        val callStub = if (child is StubBasedPsiElement<*>) (child as StubBasedPsiElement<*>).stub else null
        if (callStub != null) {
          for (callChildStub in callStub.childrenStubs) {
            val callChild = callChildStub.psi
            if (callChild is JSObjectLiteralExpression) {
              return callChild
            }
          }
        }
        else {
          return child.arguments.firstOrNull() as? JSObjectLiteralExpression
        }
        break
      }
      else if (child is JSObjectLiteralExpression) {
        return child
      }
    }
    return null
  }

  @StubUnsafe
  @JvmStatic
  fun getReferencedObjectLiteralInitializer(decorator: ES6Decorator): JSObjectLiteralExpression? {
    return AstLoadingFilter.forceAllowTreeLoading<JSObjectLiteralExpression, RuntimeException>(decorator.containingFile) {
      decorator
        .expression
        ?.asSafely<JSCallExpression>()
        ?.arguments?.firstOrNull()
        ?.asSafely<JSReferenceExpression>()
        ?.resolve()
        ?.asSafely<JSVariable>()
        ?.initializerOrStub
        ?.asSafely<JSObjectLiteralExpression>()
    }
  }

  @StubSafe
  @JvmStatic
  fun getProperty(decorator: ES6Decorator?, name: String): JSProperty? {
    return getObjectLiteralInitializer(decorator)?.findProperty(name)
  }

  @JvmStatic
  fun isAngularEntityDecorator(decorator: ES6Decorator, vararg names: String): Boolean {
    val decoratorName = decorator.decoratorName
    return (decoratorName != null
            && contains(decoratorName, *names)
            && (decoratorName != DIRECTIVE_DEC || getObjectLiteralInitializer(decorator) != null)
            && (getClassForDecoratorElement(decorator)
      ?.attributeList?.hasModifier(JSAttributeList.ModifierType.ABSTRACT) != true)
           )
           && Angular2LangUtil.isAngular2Context(decorator)
           && hasAngularImport(decoratorName, decorator.containingFile)
  }

  private fun hasAngularImport(name: String, file: PsiFile): Boolean {
    return JSStubBasedPsiTreeUtil.resolveLocally(name, file)
             ?.let { getContextOfType(it, ES6ImportDeclaration::class.java) }
             ?.fromClause
             ?.referenceText
             ?.let { StringUtil.unquoteString(it) }
             ?.let { from -> ANGULAR_CORE_PACKAGE == from }
           ?: false
  }

  @JvmStatic
  fun getClassForDecoratorElement(element: PsiElement?): TypeScriptClass? {
    val decorator = element.asSafely<ES6Decorator>()
                    ?: getContextOfType(element, ES6Decorator::class.java, false)
                    ?: return null
    val context = getContextOfType(decorator, JSAttributeListOwner::class.java) ?: return null
    return context.asSafely<TypeScriptClass>()
           ?: JSStubBasedPsiTreeUtil.getChildrenByType(context, Angular2IndexingHandler.TS_CLASS_TOKENS)
             .firstOrNull() as? TypeScriptClass
  }
}
