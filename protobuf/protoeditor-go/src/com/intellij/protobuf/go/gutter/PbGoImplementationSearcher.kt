package com.intellij.protobuf.go.gutter

import com.goide.stubs.index.GoTypesIndex
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

internal class PbGoImplementationSearcher : PbCodeImplementationSearcher {
  override fun findImplementationsForProtoElement(pbElement: PbElement,
                                                  converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    return when (pbElement) {
      is PbMessageDefinition -> handleMessageImplementations(pbElement)
      else -> emptySequence()
    }
  }

  override fun findDeclarationsForCodeElement(psiElement: PsiElement,
                                              converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    return emptySequence()
  }

  private fun handleMessageImplementations(pbElement: PbMessageDefinition): Sequence<PsiElement> {
    val goPackage = pbElement.pbFile.packageQualifiedName.toString()
    val messageName = pbElement.name ?: return emptySequence()

    return GoTypesIndex.find(messageName, pbElement.project, GlobalSearchScope.projectScope(pbElement.project), null)
      .asSequence()
      .filter {
        val packageName = it.containingFile.packageName
        packageName.isNullOrBlank() || packageName == goPackage
      }
  }
}