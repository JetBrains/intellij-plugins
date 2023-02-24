package com.intellij.protobuf.go.gutter

import com.goide.GoLanguage
import com.goide.go.GoGotoUtil
import com.goide.go.GoInheritorsSearch
import com.goide.psi.GoMethodDeclaration
import com.goide.psi.GoMethodSpec
import com.goide.psi.GoTypeSpec
import com.goide.stubs.index.GoTypesIndex
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.lang.psi.*
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor
import com.intellij.util.containers.sequenceOfNotNull

internal class PbGoImplementationSearcher : PbCodeImplementationSearcher {
  override fun findImplementationsForProtoElement(pbElement: PbElement,
                                                  converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    return when (pbElement) {
      is PbMessageDefinition -> findMessageImplementations(pbElement)
      is PbServiceDefinition -> findServiceImplementations(pbElement, converters)
      is PbServiceMethod -> findMethodImplementations(pbElement, converters)
      else -> emptySequence()
    }
  }

  override fun findDeclarationsForCodeElement(psiElement: PsiElement,
                                              converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    return when {
      psiElement.language != GoLanguage.INSTANCE -> emptySequence()
      psiElement is GoMethodDeclaration -> findMethodDeclarations(psiElement, converters)
      psiElement is GoMethodSpec -> findMethodDeclarations(psiElement, converters)
      psiElement is GoTypeSpec && hasGrpcSpecificUnimplementedMethod(psiElement) -> findServiceDeclarations(psiElement, converters)
      psiElement is GoTypeSpec -> findMessageDeclarations(psiElement)
      else -> emptySequence()
    }
  }

  private fun hasGrpcSpecificUnimplementedMethod(typeSpec: GoTypeSpec): Boolean {
    val specName = typeSpec.name ?: return false
    return typeSpec.allMethods.any { it.name == "mustEmbedUnimplemented$specName" }
  }

  private fun findGeneratedServices(serviceDefinition: PbServiceDefinition,
                                    converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    val serviceName = serviceDefinition.name ?: return emptySequence()
    val goPackage = suggestGoPackage(serviceDefinition.pbFile)
    return converters.asSequence()
      .map { converter -> converter.protoToCodeEntityName(serviceName) }
      .flatMap { maybeExistingCodeEntity ->
        findTypeSpecsWithName(maybeExistingCodeEntity, goPackage, serviceDefinition.project)
      }
  }

  private fun findServiceImplementations(serviceDefinition: PbServiceDefinition,
                                         converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    val generatedServices = findGeneratedServices(serviceDefinition, converters)
    return generatedServices
      .flatMap { typeSpec ->
        findAllCancellable { processor ->
          GoInheritorsSearch.INHERITORS_SEARCH.processQuery(GoGotoUtil.param(typeSpec), processor)
        }
      }
      .plus(generatedServices)
  }

  private fun findMethodImplementations(methodDefinition: PbServiceMethod,
                                        converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    val serviceDefinition = methodDefinition.parentOfType<PbServiceDefinition>() ?: return emptySequence()
    val methodName = methodDefinition.name ?: return emptySequence()
    val generatedMethods = findGeneratedServices(serviceDefinition, converters)
      .filterIsInstance<GoTypeSpec>()
      .flatMap { typeSpec -> typeSpec.allMethods.filter { method -> method.name == methodName } }
    return generatedMethods
      .flatMap { method ->
        findAllCancellable { processor ->
          GoInheritorsSearch.METHOD_INHERITORS_SEARCH.processQuery(GoGotoUtil.param(method), processor)
        }
      }
      .plus(generatedMethods)
  }


  private fun findAllCancellable(filter: (PsiElement) -> Boolean = { true },
                                 search: (Processor<PsiElement>) -> Unit): Sequence<PsiElement> {
    val processor = object : CommonProcessors.CollectProcessor<PsiElement>(mutableListOf<PsiElement>()) {
      override fun accept(psiElement: PsiElement?): Boolean {
        ProgressManager.checkCanceled()
        return psiElement != null && filter(psiElement) && super.accept(psiElement)
      }
    }
    search(processor)
    return processor.results.asSequence()
  }

  private fun findMessageImplementations(messageDefinition: PbMessageDefinition): Sequence<PsiElement> {
    val goPackage = suggestGoPackage(messageDefinition.pbFile)
    val messageName = messageDefinition.name ?: return emptySequence()
    return findTypeSpecsWithName(messageName, goPackage, messageDefinition.project)
  }

  private fun suggestGoPackage(pbFile: PbFile): String {
    val explicitGoPackage = pbFile.options
      .firstOrNull { it.optionName.text == PB_GO_PACKAGE_OPTION }
      ?.stringValue
      ?.asString
      ?.substringAfterLast("/")
    return explicitGoPackage ?: pbFile.packageQualifiedName.toString()
  }

  private fun findTypeSpecsWithName(name: String, goPackageName: String, project: Project): Sequence<GoTypeSpec> {
    return GoTypesIndex.find(name, project, GlobalSearchScope.projectScope(project), null)
      .asSequence()
      .filter {
        val packageName = it.containingFile.packageName
        packageName.isNullOrBlank() || packageName == goPackageName
      }
  }

  private fun findMethodDeclarations(methodSpec: GoMethodSpec,
                                     converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    val methodName = methodSpec.name ?: return emptySequence()
    val parentTypeSpec = methodSpec.parentOfType<GoTypeSpec>() ?: return emptySequence()
    return findMethodByContainingServiceDeclaration(parentTypeSpec, converters, methodName)
  }

  private fun findMethodDeclarations(methodDeclaration: GoMethodDeclaration,
                                     converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    val methodName = methodDeclaration.name ?: return emptySequence()
    val resolvedTypeSpec = methodDeclaration.resolveTypeSpec() ?: return emptySequence()
    return findMethodByContainingServiceDeclaration(resolvedTypeSpec, converters, methodName)
  }

  private fun findMethodByContainingServiceDeclaration(resolvedTypeSpec: GoTypeSpec,
                                                       converters: Collection<PbGeneratedCodeConverter>,
                                                       methodName: String): Sequence<PbServiceMethod> {
    return findServiceDeclarations(resolvedTypeSpec, converters)
      .mapNotNull { service -> service.body }
      .flatMap { body -> body.serviceMethodList }
      .filter { method -> method.name == methodName }

  }

  private fun findMessageDeclarations(typeSpec: GoTypeSpec): Sequence<PbElement> {
    val specName = typeSpec.name ?: return emptySequence()
    val goPackage = typeSpec.containingFile.packageName
    val pbMessageFqn = if (goPackage.isNullOrBlank()) specName else "$goPackage.$specName"
    val messageDefinition = typeSpec.project.service<ProtoFileAccessor>().findMessageByFqn(pbMessageFqn)
    return sequenceOfNotNull(messageDefinition)
  }

  private fun findServiceDeclarations(typeSpec: GoTypeSpec,
                                      converters: Collection<PbGeneratedCodeConverter>): Sequence<PbServiceDefinition> {
    if (!hasGrpcSpecificUnimplementedMethod(typeSpec)) return emptySequence()
    val specFqn = assembleProtoFqnBySpec(typeSpec) ?: return emptySequence()
    val protoFileAccessor = typeSpec.project.service<ProtoFileAccessor>()
    return converters.asSequence()
      .map { converter -> converter.codeEntityNameToProtoName(specFqn) }
      .mapNotNull { maybeServiceFqn -> protoFileAccessor.findServiceByFqn(maybeServiceFqn) }
  }

  private fun assembleProtoFqnBySpec(typeSpec: GoTypeSpec): String? {
    val specName = typeSpec.name ?: return null
    val goPackage = typeSpec.containingFile.packageName
    return if (goPackage.isNullOrBlank()) specName else "$goPackage.$specName"
  }
}

private const val PB_GO_PACKAGE_OPTION = "go_package"