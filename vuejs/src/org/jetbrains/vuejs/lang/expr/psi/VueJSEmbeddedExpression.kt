// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi

import com.intellij.javascript.web.lang.js.WebSymbolsExpressionWithExpectedTypeHolder
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSSourceElement

interface VueJSEmbeddedExpression : JSSourceElement, JSEmbeddedContent, WebSymbolsExpressionWithExpectedTypeHolder
