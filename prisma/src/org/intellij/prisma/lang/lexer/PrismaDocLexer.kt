package org.intellij.prisma.lang.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import org.intellij.prisma.lang.psi.DOC_COMMENT_BODY

class PrismaDocLexer : MergingLexerAdapter(FlexAdapter(_PrismaDocLexer()), TokenSet.create(DOC_COMMENT_BODY))