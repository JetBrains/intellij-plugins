// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.inspections.actionscript

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSHighlightingHandlersFactory
import com.intellij.lang.javascript.validation.ActionScriptKeywordHighlighterVisitor
import com.intellij.lang.javascript.highlighting.JSKeywordHighlighterVisitor

class ActionScriptHighlightingHandlersFactory: JSHighlightingHandlersFactory() {
  override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder, dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor {
    return ActionScriptKeywordHighlighterVisitor(holder)
  }
}