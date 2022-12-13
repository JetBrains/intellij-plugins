package org.intellij.prisma.ide.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import org.intellij.prisma.lang.psi.PRISMA_STRINGS

class PrismaQuoteHandler : SimpleTokenSetQuoteHandler(PRISMA_STRINGS)