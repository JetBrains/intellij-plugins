// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.tree.ILazyParseableElementType
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.lexer.PrismaDocLexer

class PrismaDocCommentElementType : ILazyParseableElementType("DOC_COMMENT", PrismaLanguage) {
  override fun parseContents(chameleon: ASTNode): ASTNode? {
    val builder = PsiBuilderFactory.getInstance().createBuilder(chameleon.treeParent.psi.project,
                                                                chameleon,
                                                                PrismaDocLexer(),
                                                                language,
                                                                chameleon.chars)
    doParse(builder)
    return builder.treeBuilt.firstChildNode
  }

  fun doParse(builder: PsiBuilder) {
    val root = builder.mark()

    while (!builder.eof()) {
      builder.advanceLexer()
    }

    root.done(this)
  }
}