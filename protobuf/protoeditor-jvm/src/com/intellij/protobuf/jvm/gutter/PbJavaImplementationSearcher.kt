package com.intellij.protobuf.jvm.gutter

import com.intellij.openapi.components.service
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.jvm.PbJavaFindUsagesHandlerFactory.ProtoToJavaConverter
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.protobuf.lang.psi.*
import com.intellij.protobuf.lang.stub.PbSearchParameters
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.CommonProcessors
import com.intellij.util.castSafelyTo

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
      psiElement is PsiClass && hasServiceSuperclass(psiElement, converters) -> handleService(psiElement, converters)
      psiElement is PsiClass -> handleModel(psiElement)
      psiElement is PsiMethod -> handleMethod(psiElement, converters)
      else -> emptySequence()
    }
  }

  private fun handleServiceImplementations(serviceDefinition: PbServiceDefinition,
                                           converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiClass> {
    val serviceFqn = effectiveServiceFqn(serviceDefinition) ?: return emptySequence()
    val project = serviceDefinition.project
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
    val generatedBaseClass = findGeneratedServiceSuperclass(psiClass, converters) ?: return emptySequence()
    val protoFileAccessor = psiClass.project.service<ProtoFileAccessor>()
    return protoNamesForClass(generatedBaseClass, converters)
      .flatMap { protoFileAccessor.findServicesByFqn(it, PbSearchParameters.CONTAINS) }
  }

  private fun handleMethod(psiMethod: PsiMethod, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    val containingClass = psiMethod.containingClass ?: return emptySequence()
    val generatedBaseClass = findGeneratedServiceSuperclass(containingClass, converters) ?: return emptySequence()
    val protoNamesForMethod = protoNamesForMethod(psiMethod, generatedBaseClass, converters)
    val protoFileAccessor = psiMethod.project.service<ProtoFileAccessor>()
    return protoNamesForMethod.flatMap { protoFileAccessor.findAllMethodsWithFqnPrefix(it) }
  }

  private fun effectiveServiceFqn(serviceDefinition: PbServiceDefinition): String? {
    val pbFile = serviceDefinition.containingFile.castSafelyTo<PbFile>() ?: return null
    val javaPackageOrNull = pbFile.options
      .firstOrNull { it.optionName.text == PB_JAVA_PACKAGE_OPTION }
      ?.stringValue
      ?.asString

    return if (javaPackageOrNull == null)
      serviceDefinition.qualifiedName?.toString()
    else
      "$javaPackageOrNull.${serviceDefinition.name.orEmpty()}"
  }

  private fun protoNamesForClass(psiClass: PsiClass, converters: Collection<PbGeneratedCodeConverter>): Sequence<String> {
    val qualifiedName = psiClass.qualifiedName ?: return emptySequence()
    return converters.asSequence().map { it.codeEntityNameToProtoName(qualifiedName) }
  }

  private fun protoNamesForMethod(psiMethod: PsiMethod,
                                  generatedBaseClass: PsiClass,
                                  converters: Collection<PbGeneratedCodeConverter>): Sequence<String> {
    val methodName = psiMethod.name
    return protoNamesForClass(generatedBaseClass, converters)
      .map { "$it.$methodName" }
  }

  private fun findGeneratedServiceSuperclass(psiClass: PsiClass, converters: Collection<PbGeneratedCodeConverter>): PsiClass? {
    val searchProcessor = classWithSuitableSuperSearcher(converters)
    InheritanceUtil.processSupers(psiClass, true, searchProcessor)
    return searchProcessor.foundValue
  }

  private fun classWithSuitableSuperSearcher(converters: Collection<PbGeneratedCodeConverter>): CommonProcessors.FindFirstProcessor<PsiClass> {
    return object : CommonProcessors.FindFirstProcessor<PsiClass>() {
      override fun accept(psiClass: PsiClass): Boolean {
        return converters.any { checkJavaClassHierarchy(psiClass, it) }
      }

      private fun checkJavaClassHierarchy(psiClass: PsiClass, converter: PbGeneratedCodeConverter): Boolean {
        val bindableServiceClass = JavaPsiFacade.getInstance(psiClass.project).findClass(converter.generatedFileNameHint(),
                                                                                         psiClass.resolveScope)
        return InheritanceUtil.isInheritorOrSelf(psiClass, bindableServiceClass, false)
      }
    }
  }

  private fun hasServiceSuperclass(psiClass: PsiClass, converters: Collection<PbGeneratedCodeConverter>): Boolean {
    return converters
      .map(PbGeneratedCodeConverter::generatedFileNameHint)
      .any { InheritanceUtil.isInheritor(psiClass, it) }
  }
}

private const val PB_JAVA_PACKAGE_OPTION = "java_package"