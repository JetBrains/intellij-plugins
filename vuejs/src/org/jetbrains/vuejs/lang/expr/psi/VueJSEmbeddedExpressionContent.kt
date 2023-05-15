// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi

import com.intellij.javascript.webSymbols.WebSymbolsExpressionWithExpectedTypeHolder
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.types.JSExportScope
import com.intellij.psi.StubBasedPsiElement
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub

interface VueJSEmbeddedExpressionContent : JSEmbeddedContent, WebSymbolsExpressionWithExpectedTypeHolder,
                                           JSExportScope,
                                           StubBasedPsiElement<VueJSEmbeddedExpressionContentStub>
