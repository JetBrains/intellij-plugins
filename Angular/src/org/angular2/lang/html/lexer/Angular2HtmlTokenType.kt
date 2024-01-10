// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.psi.tree.IElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.jetbrains.annotations.NonNls

class Angular2HtmlTokenType(debugName: @NonNls String) : IElementType(debugName, Angular2HtmlLanguage.INSTANCE)