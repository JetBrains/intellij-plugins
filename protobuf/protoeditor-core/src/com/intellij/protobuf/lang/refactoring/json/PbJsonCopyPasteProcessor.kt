package com.intellij.protobuf.lang.refactoring.json

import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMessageBody
import com.intellij.protobuf.lang.psi.SyntaxLevel
import com.intellij.protobuf.lang.psi.isDeprecatedProto2Syntax
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementsAtOffsetUp
import com.intellij.util.asSafely
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

internal class PbJsonCopyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData?>() {
  override fun requiresAllDocumentsToBeCommitted(editor: Editor, project: Project): Boolean {
    return false
  }

  override fun collectTransferableData(file: PsiFile,
                                       editor: Editor,
                                       startOffsets: IntArray,
                                       endOffsets: IntArray): List<TextBlockTransferableData> {
    return emptyList()
  }

  override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
    val copiedStringOrNull = content.getTransferData(DataFlavor.stringFlavor)
    if (copiedStringOrNull !is String || copiedStringOrNull.isBlank()) return emptyList()

    val trimmedContent = copiedStringOrNull.trim(' ', '\n', '\r', '\t')
    return if (fastCheckIsJson(trimmedContent))
      listOf(PbJsonTransferableData(trimmedContent))
    else
      emptyList()
  }

  override fun processTransferableData(project: Project,
                                       editor: Editor,
                                       bounds: RangeMarker,
                                       caretOffset: Int,
                                       indented: Ref<in Boolean>,
                                       values: MutableList<out TextBlockTransferableData?>) {
    val virtualFile = editor.virtualFile ?: return
    if (virtualFile.fileType != PbFileType.INSTANCE) return
    val pbFile = PsiManager.getInstance(project).findFile(virtualFile).asSafely<PbFile>() ?: return
    val syntaxLevel = pbFile.syntaxLevel
    val data = values.filterIsInstance<PbJsonTransferableData>().singleOrNull() ?: return

    val parsedJsonStruct = tryBuildStructFromJson(data.maybeJson) ?: return
    val namesScope = collectExistingNamesScope(pbFile, caretOffset) ?: return
    val protobufToInsert = assembleProtobufFile(namesScope, parsedJsonStruct, syntaxLevel)

    val document = editor.document
    PsiDocumentManager.getInstance(project).commitDocument(document)
    ApplicationManager.getApplication().runWriteAction {
      document.replaceString(bounds.startOffset, bounds.endOffset, protobufToInsert)
      ReformatCodeProcessor(pbFile, true).run()
    }
  }

  private fun assembleProtobufFile(namesScope: List<String>, parsedJsonStruct: Struct, syntaxLevel: SyntaxLevel): String {
    return with(PbJsonStructTransformer(namesScope)) {
      flattenNestedStructs(parsedJsonStruct)
        .mapNotNull(::rememberUniqueStructOrNull)
        .map { struct ->
          PbPastedEntity.PbStruct(
            getUniqueName(struct),
            mapStructFields(struct, syntaxLevel, ::getUniqueName)
          )
        }.joinToString(transform = PbPastedEntity::render, separator = "\n")
    }
  }

  private fun flattenNestedStructs(seed: Struct): List<PbStructInJson> {
    return collectPastedStructs(PbStructInJson(DEFAULT_MESSAGE_NAME, seed)).toList()
  }

  private fun collectExistingNamesScope(containingFile: PbFile, caretOffset: Int): List<String>? {
    val containingElement =
      containingFile.elementsAtOffsetUp(caretOffset)
        .asSequence()
        .map { it.first }
        .firstOrNull { it !is LeafPsiElement }
    return when (containingElement) {
      null, is PbFile -> containingFile.fullQualifiedSymbolMap.mapNotNull { it.key.lastComponent }
      is PbMessageBody -> containingElement.run {
        listOf(enumDefinitionList, messageDefinitionList, oneofDefinitionList)
          .flatten()
          .mapNotNull { it.qualifiedName?.lastComponent }
      }
      else -> return null
    }
  }

  private fun fastCheckIsJson(content: String): Boolean {
    return content.startsWith("{") && content.endsWith("}")
  }

  private fun tryBuildStructFromJson(jsonContent: String): Struct? {
    return runCatching {
      Struct.newBuilder()
        .apply {
          JsonFormat.parser().merge(jsonContent, this)
        }.build()
    }.onFailure {
      thisLogger().warn("Error during JSON to Protobuf conversion", it)
    }.getOrNull()
  }

  private fun mapStructFields(struct: Struct,
                              syntaxLevel: SyntaxLevel,
                              structNameGetter: (Struct) -> String): List<PbPastedEntity.PbField> {
    return struct.fieldsMap.entries.mapIndexed { zeroBasedIndex, (fieldName, fieldValue) ->
      PbPastedEntity.PbField(name = fieldName,
                             isRepeated = fieldValue.hasListValue(),
                             isOptional = isDeprecatedProto2Syntax(syntaxLevel),
                             type = mapFieldType(fieldValue, structNameGetter),
                             order = zeroBasedIndex + 1)
    }
  }

  private fun mapFieldType(fieldValue: Value, structNameGetter: (Struct) -> String): String {
    return when {
             fieldValue.hasBoolValue() -> "bool"
             fieldValue.hasStringValue() -> "string"
             fieldValue.hasNumberValue() -> "uint32"
             fieldValue.hasStructValue() -> structNameGetter(fieldValue.structValue)
             fieldValue.hasListValue() -> getFirstStructInArrayOrNull(fieldValue)?.let(structNameGetter)
             else -> null
           } ?: FALLBACK_FIELD_TYPE
  }

  private fun collectPastedStructs(parentStruct: PbStructInJson): Sequence<PbStructInJson> {
    return sequenceOf(parentStruct) + findChildrenStructs(parentStruct).flatMap(::collectPastedStructs)
  }

  private fun findChildrenStructs(parent: PbStructInJson): Sequence<PbStructInJson> {
    return parent.struct.fieldsMap
      .entries.asSequence()
      .mapNotNull { (childName, childBody) ->
        when {
          childBody.hasStructValue() -> {
            PbStructInJson(childName, childBody.structValue)
          }
          childBody.hasListValue() -> {
            val maybeStruct = getFirstStructInArrayOrNull(childBody) ?: return@mapNotNull null
            PbStructInJson(childName, maybeStruct)
          }
          else -> {
            null
          }
        }
      }
  }

  private fun getFirstStructInArrayOrNull(anything: Value): Struct? {
    return anything.listValue.allFields.entries
      .asSequence()
      .flatMap { (_, maybeCollection) -> maybeCollection.asSafely<Collection<Value>>() ?: emptyList() }
      .firstOrNull(Value::hasStructValue)
      ?.structValue
  }
}

private class PbJsonTransferableData(val maybeJson: String) : TextBlockTransferableData {
  override fun getFlavor(): DataFlavor {
    return PROTOBUF_JSON_DATA_FLAVOR
  }
}

private const val DEFAULT_MESSAGE_NAME = "PastedObject"
private const val FALLBACK_FIELD_TYPE = "string"
private val PROTOBUF_JSON_DATA_FLAVOR = DataFlavor(
  PbJsonCopyPasteProcessor::class.java,
  "JSON to Protobuf converter"
)