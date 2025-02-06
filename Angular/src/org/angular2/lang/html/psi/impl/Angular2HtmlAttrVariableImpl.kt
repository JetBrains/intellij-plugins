// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.presentable.JSFormatUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.HintedReferenceHost
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.IncorrectOperationException
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.Angular2HtmlLet
import org.angular2.lang.html.psi.Angular2HtmlReference
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import org.angular2.lang.html.stub.Angular2HtmlVariableElementType
import org.angular2.lang.types.Angular2LetType
import org.angular2.lang.types.Angular2ReferenceType

class Angular2HtmlAttrVariableImpl : JSVariableImpl<JSVariableStub<JSVariable>, JSVariable>, Angular2HtmlAttrVariable, HintedReferenceHost {
  constructor(node: ASTNode) : super(node)
  constructor(stub: JSVariableStub<JSVariable>) : super(stub, Angular2HtmlStubElementTypes.REFERENCE_VARIABLE)

  override val kind: Angular2HtmlAttrVariable.Kind
    get() = (elementType as Angular2HtmlVariableElementType).kind

  override fun calculateType(): JSType {
    return when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> Angular2ReferenceType(this)
      Angular2HtmlAttrVariable.Kind.LET -> Angular2LetType(this)
    }
  }

  override fun getLanguage(): Language = Angular2Language

  override fun isLocal(): Boolean =
    when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> false
      Angular2HtmlAttrVariable.Kind.LET -> true
    }

  override fun isConst(): Boolean =
    true

  override fun hasInitializer(): Boolean =
    true

  override fun hasBlockScope(): Boolean =
    when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> false
      Angular2HtmlAttrVariable.Kind.LET -> true
    }

  override fun getDeclarationScope(): PsiElement? =
    when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> super.getDeclarationScope()
      Angular2HtmlAttrVariable.Kind.LET -> parentOfType<HtmlTag>()
    }

  override fun getUseScope(): SearchScope {
    return when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> Angular2ReferenceType.getUseScope(this)
      Angular2HtmlAttrVariable.Kind.LET -> Angular2LetType.getUseScope(this)
    }
  }

  @Throws(IncorrectOperationException::class)
  override fun delete() {
    val ref = PsiTreeUtil.findFirstParent(this) { it is Angular2HtmlReference || it is Angular2HtmlLet }
    if (ref != null) {
      ref.delete()
    }
    else {
      super.delete()
    }
  }

  override fun calcAccessType(): JSAttributeList.AccessType {
    return JSAttributeList.AccessType.PUBLIC
  }

  override fun useTypesFromJSDoc(): Boolean {
    return false
  }

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> {
    return super.getReferences()
  }

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean {
    return false
  }

  override fun toString(): String {
    var classname = "Angular2HtmlAttrVariable[$kind]"
    if (!ApplicationManager.getApplication().isUnitTestMode) {
      classname += ":"
      val name = this.name
      classname += name ?: JSFormatUtil.getAnonymousElementPresentation()
    }
    return classname
  }
}