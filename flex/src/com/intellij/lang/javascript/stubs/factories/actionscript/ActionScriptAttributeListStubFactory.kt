package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.stubs.JSAttributeListStub
import com.intellij.lang.javascript.psi.stubs.impl.ActionScriptAttributeListStubImpl
import com.intellij.lang.javascript.stubs.factories.JSAttributeListStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement


internal class ActionScriptAttributeListStubFactory : JSAttributeListStubFactory(ACTIONSCRIPT_ATTRIBUTE_LIST) {
  override fun createStub(psi: JSAttributeList, parentStub: StubElement<out PsiElement>?): JSAttributeListStub =
    ActionScriptAttributeListStubImpl(psi, parentStub, elementType)
}