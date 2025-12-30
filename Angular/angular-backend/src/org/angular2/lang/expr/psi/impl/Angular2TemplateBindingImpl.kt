// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSStatementImpl
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.ContainerUtil
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.types.Angular2TemplateBindingType

class Angular2TemplateBindingImpl(elementType: IElementType,
                                  override val key: String,
                                  override val keyKind: Angular2TemplateBinding.KeyKind,
                                  private val myName: String?)
  : JSStatementImpl(elementType), Angular2TemplateBinding {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2TemplateBinding(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override val keyElement: Angular2TemplateBindingKey?
    get() = ContainerUtil.findInstance(children, Angular2TemplateBindingKey::class.java)

  override val keyJSType: JSType?
    get() {
      if (!keyIsVar()) {
        val bindings = parent as? Angular2TemplateBindings
        if (bindings != null) {
          return Angular2TemplateBindingType(bindings, key)
        }
      }
      return null
    }

  override fun getName(): String? {
    return myName
  }

  override val variableDefinition: JSVariable?
    get() = children.firstNotNullOfOrNull { it as? JSVarStatement }
      ?.variables?.firstOrNull()

  override fun keyIsVar(): Boolean {
    return keyKind != Angular2TemplateBinding.KeyKind.BINDING
  }

  override val expression: JSExpression?
    get() = getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS)
      .find { it.elementType !== Angular2ElementTypes.TEMPLATE_BINDING_KEY }
      ?.getPsi(JSExpression::class.java)

  override fun toString(): String {
    return "Angular2TemplateBinding <$key, keyKind=$keyKind, $name>"
  }
}