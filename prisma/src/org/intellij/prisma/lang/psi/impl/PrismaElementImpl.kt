package org.intellij.prisma.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import org.intellij.prisma.lang.psi.PrismaElement

open class PrismaElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), PrismaElement