package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.psi.tree.TokenSet

class DtsQuoteHandler : SimpleTokenSetQuoteHandler(TokenSet.create(DtsTypes.DQUOTE, DtsTypes.SQUOTE))