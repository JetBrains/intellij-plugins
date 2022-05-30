package com.intellij.protobuf.ide.gutter

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName

interface PbGeneratedCodeConverterProvider {
  fun getProtoConverter(): PbGeneratedCodeConverter
  fun acceptsLanguage(language: Language): Boolean
}

interface PbGeneratedCodeConverter {
  fun protoToCodeEntityName(protoName: String): String
  fun codeEntityNameToProtoName(codeEntityName: String): String
  fun generatedFileNameHint(): String
}

internal val CONVERTER_EP_NAME =
  ExtensionPointName.create<PbGeneratedCodeConverterProvider>("com.intellij.protobuf.generatedCodeConverterProvider")

