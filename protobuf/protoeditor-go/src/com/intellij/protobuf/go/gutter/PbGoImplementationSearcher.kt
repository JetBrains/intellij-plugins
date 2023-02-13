package com.intellij.protobuf.go.gutter

import com.goide.GoLanguage
import com.goide.psi.GoTypeSpec
import com.goide.stubs.index.GoTypesIndex
import com.intellij.openapi.components.service
import com.intellij.protobuf.ide.gutter.PbCodeImplementationSearcher
import com.intellij.protobuf.ide.gutter.PbGeneratedCodeConverter
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.protobuf.lang.stub.ProtoFileAccessor
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.sequenceOfNotNull

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
    return when {
      psiElement.language != GoLanguage.INSTANCE -> emptySequence()
      psiElement is GoTypeSpec -> findMessageDeclaration(psiElement)
      else -> emptySequence()
    }
  }

  private fun handleMessageImplementations(pbElement: PbMessageDefinition): Sequence<PsiElement> {
    val explicitGoPackage = pbElement.pbFile.options
      .firstOrNull { it.optionName.text == PB_GO_PACKAGE_OPTION }
      ?.stringValue
      ?.asString
      ?.substringAfterLast("/")
    val goPackage = explicitGoPackage ?: pbElement.pbFile.packageQualifiedName.toString()
    val messageName = pbElement.name ?: return emptySequence()

    return GoTypesIndex.find(messageName, pbElement.project, GlobalSearchScope.projectScope(pbElement.project), null)
      .asSequence()
      .filter {
        val packageName = it.containingFile.packageName
        packageName.isNullOrBlank() || packageName == goPackage
      }
  }

  private fun findMessageDeclaration(typeSpec: GoTypeSpec): Sequence<PbElement> {
    val specName = typeSpec.name ?: return emptySequence()
    val goPackage = typeSpec.containingFile.packageName
    val pbMessageFqn = if (goPackage.isNullOrBlank()) specName else "$goPackage.$specName"
    val messageDefinition = typeSpec.project.service<ProtoFileAccessor>().findMessageByFqn(pbMessageFqn)
    return sequenceOfNotNull(messageDefinition)
  }
}

private const val PB_GO_PACKAGE_OPTION = "go_package"