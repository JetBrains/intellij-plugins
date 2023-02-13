package com.intellij.protobuf.ide.gutter

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.PsiElement

interface PbGeneratedCodeConverterProvider {
  fun getProtoConverter(): PbGeneratedCodeConverter
  fun acceptsLanguage(language: Language): Boolean
}

interface PbGeneratedCodeConverter {
  fun protoToCodeEntityName(protoName: String): String
  fun codeEntityNameToProtoName(codeEntityName: String): String
  fun generatedFileNameHint(): String
}

interface PbCodeImplementationSearcher {
  fun findImplementationsForProtoElement(pbElement: PbElement,
                                         converters: Collection<PbGeneratedCodeConverter>): Sequence<PsiElement>

  fun findDeclarationsForCodeElement(psiElement: PsiElement, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement>
}

internal val IMPLEMENTATION_SEARCHER_EP_NAME =
  ExtensionPointName<PbCodeImplementationSearcher>("com.intellij.protobuf.codeImplementationSearcher")

internal val CONVERTER_EP_NAME =
  ExtensionPointName.create<PbGeneratedCodeConverterProvider>("com.intellij.protobuf.generatedCodeConverterProvider")

