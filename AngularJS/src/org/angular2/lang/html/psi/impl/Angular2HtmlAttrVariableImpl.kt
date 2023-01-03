// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.presentable.JSFormatUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
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

  override fun isLocal(): Boolean {
    return false
  }

  override fun isExported(): Boolean {
    return true
  }

  override fun getUseScope(): SearchScope {
    return when (kind) {
      Angular2HtmlAttrVariable.Kind.REFERENCE -> Angular2ReferenceType.getUseScope(this)
      Angular2HtmlAttrVariable.Kind.LET -> Angular2LetType.getUseScope(this)
    }
  }

  @Throws(IncorrectOperationException::class)
  override fun delete() {
    val ref = PsiTreeUtil.findFirstParent(this) { it is Angular2HtmlReference }
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