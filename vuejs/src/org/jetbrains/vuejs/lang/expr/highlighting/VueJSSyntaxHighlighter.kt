// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.highlighting

import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import org.jetbrains.vuejs.lang.expr.VueJSLanguage


class VueJSSyntaxHighlighter : TypeScriptHighlighter(VueJSLanguage.INSTANCE.optionHolder, false)
