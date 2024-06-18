package org.intellij.prisma.lang.psi

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.intellij.prisma.lang.PrismaLanguage

private const val STUB_VERSION = 1

object PrismaFileElementType : IStubFileElementType<PsiFileStub<PrismaFile>>("PRISMA_FILE", PrismaLanguage) {
  override fun getExternalId(): String = "${language.id}.file"

  override fun getStubVersion(): Int = super.getStubVersion() + STUB_VERSION
}