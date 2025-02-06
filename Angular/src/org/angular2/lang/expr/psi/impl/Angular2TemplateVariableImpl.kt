// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates
import org.angular2.lang.expr.parser.Angular2StubElementTypes
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings

class Angular2TemplateVariableImpl : JSVariableImpl<JSVariableStub<in JSVariable>, JSVariable> {
  constructor(node: ASTNode) : super(node)
  constructor(stub: JSVariableStub<JSVariable>) : super(stub, Angular2StubElementTypes.TEMPLATE_VARIABLE)

  override fun calculateType(): JSType? {
    val bindings = PsiTreeUtil.getParentOfType(this, Angular2TemplateBindings::class.java)
    val binding = PsiTreeUtil.getParentOfType(this, Angular2TemplateBinding::class.java)
    if (binding == null || binding.name == null || bindings == null) return null

    var propertyType: JSType? = null
    val propertyName = binding.name
    if (propertyName == null) return null

    val contextType = JSResolveUtil.getElementJSType(bindings)
    if (contextType != null) {
      val signature = contextType.asRecordType(this).findPropertySignature(propertyName)
      propertyType = signature?.jsType
    }
    if (propertyType == null || propertyType is JSAnyType) {
      if (!isStrictTemplates(this)) {
        for (candidate in bindings.bindings) {
          if (candidate !== binding && !candidate.keyIsVar() && propertyName == candidate.key) {
            propertyType = JSResolveUtil.getExpressionJSType(candidate.expression)
            break
          }
        }
      }
    }

    return propertyType?.substitute(this)
  }

  override fun getJSType(): JSType? {
    return CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(calculateType(), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  override fun delete() {
    parent.asSafely<JSVarStatement>()
      ?.parent?.asSafely<Angular2TemplateBinding>()
      ?.delete()
  }

  override fun hasBlockScope(): Boolean =
    true

  override fun getDeclarationScope(): PsiElement? =
    parentOfType<HtmlTag>()

  override fun isLocal(): Boolean {
    return true
  }

  override fun isConst(): Boolean {
    return true
  }

  override fun hasInitializer(): Boolean {
    return true
  }

  override fun calcAccessType(): JSAttributeList.AccessType {
    return JSAttributeList.AccessType.PUBLIC
  }

  override fun useTypesFromJSDoc(): Boolean {
    return false
  }
}