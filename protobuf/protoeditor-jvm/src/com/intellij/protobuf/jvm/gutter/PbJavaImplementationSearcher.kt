package com.intellij.protobuf.jvm.gutter

import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

internal class PbJavaImplementationSearcher : PbCodeImplementationSearcher {
  override fun findImplementationsForProtoElement(pbElement: PbElement): Sequence<NavigatablePsiElement> {
    return emptySequence()
  }

  override fun findDeclarationsForCodeElement(psiElement: PsiElement): Sequence<PbElement> {
    return when {
      psiElement is PsiClass && hasServiceSuperclass(psiElement) -> handleService(psiElement)
      psiElement is PsiClass -> handleModel(psiElement)
      psiElement is PsiMethod -> handleMethod(psiElement)
      else -> emptySequence()
    }
  }

  private fun handleModel(psiElement: PsiElement): Sequence<PbElement> {
    return PbJavaGotoDeclarationHandler.findProtoDeclarationForJavaElement(psiElement)
      ?.asSequence()
      .orEmpty()
      .filterIsInstance<PbElement>()
  }

  private fun handleService(psiClass: PsiClass): Sequence<PbElement> {
    return emptySequence() //todo
  }

  private fun handleMethod(psiMethod: PsiMethod): Sequence<PbElement> {
    return emptySequence() //todo
  }

  private fun hasServiceSuperclass(psiClass: PsiClass): Boolean {
    return psiClass.supers.any(::isServiceSuperclass)
  }

  private fun isServiceSuperclass(psiClass: PsiClass): Boolean {
    // todo
    return psiClass.qualifiedName == "io.grpc.BindableService"
  }
}