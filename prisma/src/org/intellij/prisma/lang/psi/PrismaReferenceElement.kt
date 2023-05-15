package org.intellij.prisma.lang.psi

import com.intellij.psi.PsiElement
import org.intellij.prisma.lang.resolve.PrismaReference

interface PrismaReferenceElement : PrismaElement {

  val referenceNameElement: PsiElement?

  val referenceName: String?
    get() = referenceNameElement?.text

  override fun getReference(): PrismaReference?

  fun resolve(): PsiElement?

}