package com.intellij.protobuf.jvm.gutter

import com.intellij.openapi.components.service
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.CommonProcessors

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
    return PbJavaGotoDeclarationHandler.findProtoDeclarationForResolvedJavaElement(psiElement)
      ?.asSequence()
      .orEmpty()
      .filterIsInstance<PbElement>()
  }

  private fun handleService(psiClass: PsiClass): Sequence<PbElement> {
    val generatedBaseClass = findGeneratedServiceSuperclass(psiClass) ?: return emptySequence()
    val protoServiceFqn = protoNameForClass(generatedBaseClass) ?: return emptySequence()
    return psiClass.project.service<ProtoFileAccessor>().findServicesByFqn(protoServiceFqn, true)
  }

  private fun handleMethod(psiMethod: PsiMethod): Sequence<PbElement> {
    val containingClass = psiMethod.containingClass ?: return emptySequence()
    val generatedBaseClass = findGeneratedServiceSuperclass(containingClass) ?: return emptySequence()
    val protoNameForMethod = protoNameForMethod(psiMethod, generatedBaseClass) ?: return emptySequence()
    return psiMethod.project.service<ProtoFileAccessor>().findAllMethodsWithFqnPrefix(protoNameForMethod)
  }

  private fun protoNameForClass(psiClass: PsiClass): String? {
    return psiClass.qualifiedName?.substringBefore("Grpc") //todo: should be extendable mechanism (not only grpc might be generated)
  }

  private fun protoNameForMethod(psiMethod: PsiMethod, generatedBaseClass: PsiClass): String? {
    val protoNameForClass = protoNameForClass(generatedBaseClass) ?: return null
    val methodName = psiMethod.name
    return "$protoNameForClass.$methodName"
  }

  private fun findGeneratedServiceSuperclass(psiClass: PsiClass): PsiClass? {
    val findFirstProcessor = object : CommonProcessors.FindFirstProcessor<PsiClass>() {
      override fun accept(psiClass: PsiClass): Boolean {
        val bindableServiceClass = JavaPsiFacade.getInstance(psiClass.project).findClass(BINDABLE_SERVICE_FQN, psiClass.resolveScope)
        return InheritanceUtil.isInheritorOrSelf(psiClass, bindableServiceClass, false)
      }
    }
    InheritanceUtil.processSupers(psiClass, true, findFirstProcessor)
    return findFirstProcessor.foundValue
  }

  private fun hasServiceSuperclass(psiClass: PsiClass): Boolean {
    return InheritanceUtil.isInheritor(psiClass, BINDABLE_SERVICE_FQN)
  }
}

private const val BINDABLE_SERVICE_FQN = "io.grpc.BindableService"