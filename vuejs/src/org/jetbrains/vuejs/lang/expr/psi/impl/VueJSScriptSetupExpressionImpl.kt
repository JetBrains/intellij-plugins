// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSParameterList
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.psi.VueJSScriptSetupExpression

open class VueJSScriptSetupExpressionImpl(vueJSElementType: IElementType) : JSExpressionImpl(vueJSElementType), VueJSScriptSetupExpression {

  override fun getParameterList(): JSParameterList? =
    firstChild as? JSParameterList

}
