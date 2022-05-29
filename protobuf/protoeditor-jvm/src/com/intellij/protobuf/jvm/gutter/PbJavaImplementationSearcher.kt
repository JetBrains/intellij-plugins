package com.intellij.protobuf.jvm.gutter

import com.intellij.openapi.components.service
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.jvm.PbJavaFindUsagesHandlerFactory.ProtoToJavaConverter
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.protobuf.lang.psi.*
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.CommonProcessors

internal class PbJavaImplementationSearcher : PbCodeImplementationSearcher {
  override fun findImplementationsForProtoElement(pbElement: PbElement,
                                                  converters: Collection<PbGeneratedCodeConverter>): Sequence<NavigatablePsiElement> {
    return when (pbElement) {
      is PbServiceDefinition -> handleServiceImplementations(pbElement, converters)
      is PbServiceMethod -> handleMethodImplementations(pbElement, converters)
      is PbMessageDefinition -> handleMessageImplementations(pbElement)
      else -> emptySequence()
    }
  }

  override fun findDeclarationsForCodeElement(psiElement: PsiElement,
                                              converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    return when {
      psiElement is PsiClass && hasServiceSuperclass(psiElement) -> handleService(psiElement, converters)
      psiElement is PsiClass -> handleModel(psiElement)
      psiElement is PsiMethod -> handleMethod(psiElement, converters)
      else -> emptySequence()
    }
  }

  //todo pass only applicable converter -> add language ID to implementation searcher??
  private fun handleServiceImplementations(serviceDefinition: PbServiceDefinition,
                                           converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiClass> {
    val serviceFqn = serviceDefinition.qualifiedName?.toString() ?: return emptySequence()
    val project = serviceDefinition.project
    // todo consider java_package option?
    return converters.asSequence()
      .map { converter -> converter.protoToCodeEntityName(serviceFqn) }
      .mapNotNull { maybeExistingCodeEntity ->
        JavaPsiFacade.getInstance(project).findClass(maybeExistingCodeEntity, GlobalSearchScope.projectScope(project))
      }
      .flatMap { baseClass -> ClassInheritorsSearch.search(baseClass) }
  }

  private fun handleMethodImplementations(methodDefinition: PbServiceMethod,
                                          converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiMethod> {
    val serviceDefinition = methodDefinition.parentOfType<PbServiceDefinition>() ?: return emptySequence()
    val methodName = methodDefinition.name ?: return emptySequence()
    return handleServiceImplementations(serviceDefinition, converters)
      .flatMap { it.methods.asSequence() }
      .filter { it.name == methodName }
  }

  private fun handleMessageImplementations(messageDefinition: PbMessageDefinition): Sequence<PsiClass> {
    val pbFile = messageDefinition.parentOfType<PbFile>() ?: return emptySequence()
    val dispatcher = ProtoToJavaConverter(pbFile)
    messageDefinition.accept(dispatcher)
    return dispatcher.results?.asSequence().orEmpty().filterIsInstance<PsiClass>()
  }

  private fun handleModel(psiElement: PsiElement): Sequence<PbElement> {
    return PbJavaGotoDeclarationHandler.findProtoDeclarationForResolvedJavaElement(psiElement)
      ?.asSequence()
      .orEmpty()
      .filterIsInstance<PbElement>()
  }

  private fun handleService(psiClass: PsiClass, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    val generatedBaseClass = findGeneratedServiceSuperclass(psiClass) ?: return emptySequence()
    val protoServiceFqn = protoNameForClass(generatedBaseClass, converters) ?: return emptySequence()
    return psiClass.project.service<ProtoFileAccessor>().findServicesByFqn(protoServiceFqn, true)
  }

  private fun handleMethod(psiMethod: PsiMethod, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    val containingClass = psiMethod.containingClass ?: return emptySequence()
    val generatedBaseClass = findGeneratedServiceSuperclass(containingClass) ?: return emptySequence()
    val protoNameForMethod = protoNameForMethod(psiMethod, generatedBaseClass, converters) ?: return emptySequence()
    return psiMethod.project.service<ProtoFileAccessor>().findAllMethodsWithFqnPrefix(protoNameForMethod)
  }

  private fun protoNameForClass(psiClass: PsiClass, converters: Collection<PbGeneratedCodeConverter>): String? {
    val qualifiedName = psiClass.qualifiedName ?: return null
    val languageConverter = converters.firstOrNull { it.acceptsLanguage(psiClass.language) } ?: return null
    return languageConverter.codeEntityNameToProtoName(qualifiedName)
  }

  private fun protoNameForMethod(psiMethod: PsiMethod,
                                 generatedBaseClass: PsiClass,
                                 converters: Collection<PbGeneratedCodeConverter>): String? {
    val protoNameForClass = protoNameForClass(generatedBaseClass, converters) ?: return null
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