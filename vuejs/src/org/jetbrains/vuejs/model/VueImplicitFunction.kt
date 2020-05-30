// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSParameterItem
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitFunctionImpl
import com.intellij.psi.PsiElement

class VueImplicitFunction(name: String, returnType: JSType?,
                          provider: PsiElement, parameters: List<JSParameterItem>) :
  JSLocalImplicitFunctionImpl(name, returnType, provider, *parameters.toTypedArray()) {

}