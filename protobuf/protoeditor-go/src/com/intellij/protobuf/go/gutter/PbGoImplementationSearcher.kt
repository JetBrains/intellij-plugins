package com.intellij.protobuf.go.gutter

import com.goide.GoLanguage
import com.goide.psi.GoTypeSpec
import com.goide.stubs.index.GoTypesIndex
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.protobuf.lang.psi.PbServiceDefinition
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.sequenceOfNotNull

internal class PbGoImplementationSearcher : PbCodeImplementationSearcher {
  override fun findImplementationsForProtoElement(pbElement: PbElement,
                                                  converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    return when (pbElement) {
      is PbMessageDefinition -> findMessageImplementations(pbElement)
      is PbServiceDefinition -> findServiceImplementations(pbElement, converters)
      else -> emptySequence()
    }
  }

  override fun findDeclarationsForCodeElement(psiElement: PsiElement,
                                              converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
    return when {
      psiElement.language != GoLanguage.INSTANCE -> emptySequence()
      psiElement is GoTypeSpec && hasGrpcSpecificUnimplementedMethod(psiElement) -> findServiceDeclaration(psiElement, converters)
      psiElement is GoTypeSpec -> findMessageDeclaration(psiElement)
      else -> emptySequence()
    }
  }

  private fun hasGrpcSpecificUnimplementedMethod(typeSpec: GoTypeSpec): Boolean {
    val specName = typeSpec.name ?: return false
    return typeSpec.allMethods.any { it.name == "mustEmbedUnimplemented$specName" }
  }

  private fun findServiceImplementations(serviceDefinition: PbServiceDefinition, converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement> {
    val serviceName = serviceDefinition.name ?: return emptySequence()
    val goPackage = suggestGoPackage(serviceDefinition.pbFile)
    return converters.asSequence()
      .map { converter -> converter.protoToCodeEntityName(serviceName) }
      .flatMap { maybeExistingCodeEntity ->
        findTypeSpecsWithName(maybeExistingCodeEntity, goPackage, serviceDefinition.project)
      }
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

  private fun findTypeSpecsWithName(name: String, goPackageName: String, project: Project): Sequence<PsiElement> {
    return GoTypesIndex.find(name, project, GlobalSearchScope.projectScope(project), null)
      .asSequence()
      .filter {
        val packageName = it.containingFile.packageName
        packageName.isNullOrBlank() || packageName == goPackageName
      }
  }

  private fun findMessageDeclaration(typeSpec: GoTypeSpec): Sequence<PbElement> {
    val specName = typeSpec.name ?: return emptySequence()
    val goPackage = typeSpec.containingFile.packageName
    val pbMessageFqn = if (goPackage.isNullOrBlank()) specName else "$goPackage.$specName"
    val messageDefinition = typeSpec.project.service<ProtoFileAccessor>().findMessageByFqn(pbMessageFqn)
    return sequenceOfNotNull(messageDefinition)
  }

  private fun findServiceDeclaration(typeSpec: GoTypeSpec, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement> {
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