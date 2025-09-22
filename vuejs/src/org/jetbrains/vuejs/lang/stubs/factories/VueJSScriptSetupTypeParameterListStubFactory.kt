// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.stubs.TypeScriptTypeParameterListStub
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.SCRIPT_SETUP_TYPE_PARAMETER_LIST
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSScriptSetupTypeParameterListStubImpl

internal class VueJSScriptSetupTypeParameterListStubFactory : JSStubFactory<TypeScriptTypeParameterListStub, TypeScriptTypeParameterList>(SCRIPT_SETUP_TYPE_PARAMETER_LIST) {
  override fun createStub(psi: TypeScriptTypeParameterList, parentStub: StubElement<out PsiElement>?): TypeScriptTypeParameterListStub =
    VueJSScriptSetupTypeParameterListStubImpl(psi, parentStub, elementType)
}