// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi

import com.intellij.lang.javascript.psi.JSCallExpression

interface VueJSFilterExpression : JSCallExpression {

  override fun getName(): String?

  val filterArgumentsList: VueJSFilterArgumentsList?

}
