package org.intellij.prisma.lang.psi

import com.intellij.psi.tree.IStubFileElementType
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.stubs.PrismaFileStub

private const val STUB_VERSION = 3

object PrismaFileElementType : IStubFileElementType<PrismaFileStub>("PRISMA_FILE", PrismaLanguage) {
  override fun getExternalId(): String = "${language.id}.file"

  override fun getStubVersion(): Int = super.getStubVersion() + STUB_VERSION
}