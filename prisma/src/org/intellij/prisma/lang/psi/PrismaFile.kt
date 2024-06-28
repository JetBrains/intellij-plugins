package org.intellij.prisma.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.childrenOfType
import org.intellij.prisma.lang.PrismaFileType
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.resolve.PrismaSchemaMetadata
import org.intellij.prisma.lang.resolve.resolveSchemaMetadata

class PrismaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, PrismaLanguage) {

  override fun getFileType(): FileType = PrismaFileType

  val metadata: PrismaSchemaMetadata
    get() = resolveSchemaMetadata(this)

  val declarations: List<PrismaDeclaration>
    get() = childrenOfType()

  private val entityDeclarations: List<PrismaEntityDeclaration>
    get() = childrenOfType()

  override fun processDeclarations(
    processor: PsiScopeProcessor,
    state: ResolveState,
    lastParent: PsiElement?,
    place: PsiElement,
  ): Boolean {
    for (declaration in entityDeclarations) {
      if (!processor.execute(declaration, state)) return false
    }

    return super.processDeclarations(processor, state, lastParent, place)
  }
}