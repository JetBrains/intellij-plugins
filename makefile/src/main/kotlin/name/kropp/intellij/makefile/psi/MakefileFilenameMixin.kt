package name.kropp.intellij.makefile.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*

open class MakefileFilenameMixin internal constructor(astNode: ASTNode) : ASTWrapperPsiElement(astNode) {
  override fun getReferences(): Array<out FileReference> = FileReferenceSet(node.psi).allReferences
}
