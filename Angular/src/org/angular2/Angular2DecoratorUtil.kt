// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.injections.JSInjectionUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.util.PsiTreeUtil.getContextOfType
import com.intellij.psi.util.PsiTreeUtil.getStubChildrenOfTypeAsList
import com.intellij.psi.util.parentOfTypes
import com.intellij.util.ArrayUtil.contains
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.index.TS_CLASS_TOKENS
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE

object Angular2DecoratorUtil {
  const val DIRECTIVE_DEC: String = "Directive"
  const val COMPONENT_DEC: String = "Component"
  const val PIPE_DEC: String = "Pipe"
  const val MODULE_DEC: String = "NgModule"
  const val INPUT_DEC: String = "Input"
  const val INPUT_FUN: String = "input"
  const val OUTPUT_DEC: String = "Output"
  const val OUTPUT_FUN: String = "output"
  const val OUTPUT_FROM_OBSERVABLE_FUN: String = "outputFromObservable"
  const val MODEL_FUN: String = "model"
  const val FORWARD_REF_FUN: String = "forwardRef"
  const val INJECT_FUN: String = "inject"
  const val ATTRIBUTE_DEC: String = "Attribute"
  const val VIEW_CHILD_DEC: String = "ViewChild"
  const val VIEW_CHILDREN_DEC: String = "ViewChildren"
  const val VIEW_DEC: String = "View"
  const val OPTIONAL_DEC: String = "Optional"
  const val NAME_PROP: String = "name"
  const val SELECTOR_PROP: String = "selector"
  const val EXPORT_AS_PROP: String = "exportAs"
  const val INPUTS_PROP: String = "inputs"
  const val OUTPUTS_PROP: String = "outputs"
  const val STANDALONE_PROP: String = "standalone"
  const val IMPORTS_PROP: String = "imports"
  const val EXPORTS_PROP: String = "exports"
  const val DECLARATIONS_PROP: String = "declarations"
  const val ENTRY_COMPONENTS_PROP: String = "entryComponents"
  const val HOST_PROP: String = "host"
  const val HOST_BINDING_DEC: String = "HostBinding"
  const val HOST_LISTENER_DEC: String = "HostListener"
  const val HOST_DIRECTIVES_PROP: String = "hostDirectives"
  const val BOOTSTRAP_PROP: String = "bootstrap"
  const val TEMPLATE_URL_PROP: String = "templateUrl"
  const val TEMPLATE_PROP: String = "template"
  const val STYLE_URLS_PROP: String = "styleUrls"
  const val STYLE_URL_PROP: String = "styleUrl"
  const val STYLES_PROP: String = "styles"
  const val REQUIRED_PROP: String = "required"
  const val ALIAS_PROP: String = "alias"
  const val TRANSFORM_PROP: String = "transform"
  const val DIRECTIVE_PROP: String = "directive"

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
    return findDecorator(attributeListOwner, false, *names)
  }

  @StubSafe
  @JvmStatic
  fun findDecorator(attributeListOwner: JSAttributeListOwner, allowAbstractClasses: Boolean, vararg names: String): ES6Decorator? {
    val list = attributeListOwner.attributeList
    if (list == null || names.isEmpty()) {
      return null
    }
    for (decorator in getStubChildrenOfTypeAsList(list, ES6Decorator::class.java)) {
      if (isAngularEntityDecorator(decorator, allowAbstractClasses, *names)) {
        return decorator
      }
    }
    if (attributeListOwner is TypeScriptClassExpression) {
      val context = attributeListOwner.getContext() as? JSAttributeListOwner
      if (context != null) {
        return findDecorator(context, allowAbstractClasses, *names)
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
        value.stringValue
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
          return child.arguments.asSequence().map { it.unwrapParenthesis() }.firstOrNull() as? JSObjectLiteralExpression
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
        ?.unwrapParenthesis()
        ?.asSafely<JSObjectLiteralExpression>()
    }
  }

  @StubSafe
  @JvmStatic
  fun getProperty(decorator: ES6Decorator?, name: String): JSProperty? {
    return getObjectLiteralInitializer(decorator)?.findProperty(name)
  }

  @JvmStatic
  fun isAngularEntityDecorator(decorator: ES6Decorator, vararg names: String): Boolean =
    isAngularEntityDecorator(decorator, false, *names)

  @JvmStatic
  fun isAngularEntityDecorator(decorator: ES6Decorator, allowAbstractClasses: Boolean, vararg names: String): Boolean {
    val decoratorName = decorator.decoratorName
    return (decoratorName != null
            && contains(decoratorName, *names)
            && (decoratorName != DIRECTIVE_DEC || getObjectLiteralInitializer(decorator) != null)
            && (allowAbstractClasses || getClassForDecoratorElement(decorator)?.attributeList?.hasModifier(JSAttributeList.ModifierType.ABSTRACT) != true)
           )
           && Angular2LangUtil.isAngular2Context(decorator)
           && hasAngularImport(decoratorName, decorator.containingFile)
  }

  private fun hasAngularImport(name: String, file: PsiFile): Boolean =
    JSStubBasedPsiTreeUtil.resolveLocally(name, file)
      ?.let { getContextOfType(it, ES6ImportDeclaration::class.java) }
      ?.fromClause
      ?.referenceText
      ?.let { StringUtil.unquoteString(it) }
      ?.let { from -> ANGULAR_CORE_PACKAGE == from } == true

  @JvmStatic
  fun getClassForDecoratorElement(element: PsiElement?): TypeScriptClass? {
    val decorator = element.asSafely<ES6Decorator>()
                    ?: getContextOfType(element, ES6Decorator::class.java, false)
                    ?: return null
    val context = getContextOfType(decorator, JSAttributeListOwner::class.java) ?: return null
    return context.asSafely<TypeScriptClass>()
           ?: JSStubBasedPsiTreeUtil.getChildrenByType(context, TS_CLASS_TOKENS)
             .firstOrNull() as? TypeScriptClass
  }

  fun isHostBindingExpression(context: PsiElement): Boolean =
    InjectedLanguageManager.getInstance(context.project).getInjectionHost(context)?.asSafely<JSLiteralExpression>()
      ?.parent?.asSafely<JSProperty>()
      ?.let { isHostBinding(it) } == true

  fun isAngularEntityInitializerProperty(property: JSProperty, allowAbstractClasses: Boolean, vararg names: String): Boolean =
    property.parent?.asSafely<JSObjectLiteralExpression>()
      ?.parent?.asSafely<JSArgumentList>()
      ?.parent?.asSafely<JSCallExpression>()
      ?.parent?.asSafely<ES6Decorator>()
      ?.let { isAngularEntityDecorator(it, allowAbstractClasses, *names) } == true

  fun isHostBinding(property: JSProperty): Boolean =
    property.parent?.asSafely<JSObjectLiteralExpression>()
      ?.parent?.asSafely<JSProperty>()
      ?.takeIf { it.name == HOST_PROP }
      ?.let { isAngularEntityInitializerProperty(it, true, COMPONENT_DEC, DIRECTIVE_DEC) } == true

  fun getDecoratorForLiteralParameter(literal: JSLiteralExpression): ES6Decorator? =
    literal
      .parent.asSafely<JSArgumentList>()
      ?.parent.asSafely<JSCallExpression>()
      ?.parent?.asSafely<ES6Decorator>()

  fun isHostListenerDecoratorEventLiteral(literal: JSLiteralExpression): Boolean =
    literal
      .parent.asSafely<JSArgumentList>()
      ?.takeIf { it.arguments.getOrNull(0) == literal }
      ?.parentOfTypes(ES6Decorator::class, JSProperty::class, JSExecutionScope::class)
      ?.asSafely<ES6Decorator>()
      ?.decoratorName == HOST_LISTENER_DEC

  fun isHostBindingClassValueLiteral(literal: JSLiteralExpression): Boolean =
    literal.parent.asSafely<JSProperty>()?.let {
      it.value == literal && isHostBinding(it) && it.name == "class"
    } == true

  private fun JSExpression.unwrapParenthesis(): JSExpression? =
    JSUtils.unparenthesize(this)
}
