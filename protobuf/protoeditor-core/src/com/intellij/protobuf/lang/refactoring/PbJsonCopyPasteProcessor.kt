package com.intellij.protobuf.lang.refactoring

import com.google.protobuf.Struct
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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMessageBody
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementsAtOffsetUp
import com.intellij.util.asSafely
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

internal class PbJsonCopyPasteProcessor : CopyPastePostProcessor<TextBlockTransferableData?>() {
  override fun collectTransferableData(file: PsiFile,
                                       editor: Editor,
                                       startOffsets: IntArray,
                                       endOffsets: IntArray): List<TextBlockTransferableData> {
    return emptyList()
  }

  override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
    val copiedStringOrNull = content.getTransferData(DataFlavor.stringFlavor)
    if (copiedStringOrNull !is String || copiedStringOrNull.isBlank() || !fastCheckIsJson(copiedStringOrNull)) return emptyList()
    return listOf(PbJsonTransferableData(copiedStringOrNull))
  }

  override fun processTransferableData(project: Project,
                                       editor: Editor,
                                       bounds: RangeMarker,
                                       caretOffset: Int,
                                       indented: Ref<in Boolean>,
                                       values: MutableList<out TextBlockTransferableData?>) {
    if (editor.virtualFile.fileType != PbFileType.INSTANCE) return
    val pbFile = PsiManager.getInstance(project).findFile(editor.virtualFile).asSafely<PbFile>() ?: return
    val data = values.filterIsInstance<PbJsonTransferableData>().singleOrNull() ?: return
    val parsedJsonStruct = tryBuildStructFromJson(data.maybeJson) ?: return
    val namesScope = collectExistingNames(pbFile, caretOffset) ?: return

    val nameSuggester = PbNameSuggester(namesScope)
    val initialMetadata = StructMetadata(parsedJsonStruct, nameSuggester.rememberFqn(DEFAULT_MESSAGE_NAME, parsedJsonStruct))
    // write names
    val flattenStructs = collectPastedStructs(parentStruct = initialMetadata, nameSuggester = nameSuggester)
    //read names
    val protobufStructure = collectPastedEntities(flattenStructs, nameSuggester)
      .joinToString(transform = PbPastedEntity::render, separator = "\n")

    val document = editor.document
    PsiDocumentManager.getInstance(project).commitDocument(document)

    ApplicationManager.getApplication().runWriteAction {
      document.replaceString(bounds.startOffset, bounds.endOffset, protobufStructure)
      ReformatCodeProcessor(pbFile, true).run()
    }
  }

  override fun requiresAllDocumentsToBeCommitted(editor: Editor, project: Project): Boolean {
    return false
  }

  private fun collectExistingNames(containingFile: PbFile, caretOffset: Int): List<String>? {
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

  private fun collectPastedEntities(structs: Collection<StructMetadata>, nameSuggester: PbNameSuggester): List<PbPastedEntity.PbStruct> {
    return structs.distinctBy { it.struct }
      .map { structMetadata ->
        PbPastedEntity.PbStruct(
          nameSuggester.getUniqueName(structMetadata.struct),
          mapStructFields(structMetadata, nameSuggester)
        )
      }
  }

  private fun mapStructFields(structMetadata: StructMetadata, nameSuggester: PbNameSuggester): List<PbPastedEntity.PbField> {
    return structMetadata.struct.fieldsMap.entries.mapIndexed { index, (fieldName, fieldValue) ->
      val type = when {
        fieldValue.hasBoolValue() -> "bool"
        fieldValue.hasStringValue() -> "string"
        fieldValue.hasNumberValue() -> "uint32"
        fieldValue.hasStructValue() -> nameSuggester.getUniqueName(fieldValue.structValue)
        //nameSuggester.getUniqueName(structMetadata.fqn + "." + StringUtil.capitalize(fieldName))
        else -> "string"
      }
      PbPastedEntity.PbField(fieldName, fieldValue.hasListValue(), type, index + 1)
    }
  }

  private fun collectPastedStructs(parentStruct: StructMetadata, nameSuggester: PbNameSuggester): List<StructMetadata> {
    val children = findChildrenStructs(parentStruct, nameSuggester)
    return listOf(parentStruct) +
           children.flatMap { childStruct -> collectPastedStructs(childStruct, nameSuggester) }
  }

  private class StructMetadata(
    val struct: Struct,
    val fqn: String
  ) {

  }

  private fun findChildrenStructs(parent: StructMetadata, nameSuggester: PbNameSuggester): List<StructMetadata> {
    return parent.struct.fieldsMap
      .entries.asSequence()
      .filter { it.value.hasStructValue() }
      .map { (childName, childStruct) ->
        val fqn = parent.fqn + "." + StringUtil.capitalize(childName)
        nameSuggester.rememberFqn(fqn, childStruct.structValue)
        StructMetadata(childStruct.structValue, fqn)
      }
      .toList()
  }
}

private class PbJsonTransferableData(val maybeJson: String) : TextBlockTransferableData {
  override fun getFlavor(): DataFlavor {
    return PROTOBUF_JSON_DATA_FLAVOR
  }
}

private class PbNameSuggester(existingNames: Collection<String>) {
  private val names = existingNames.toMutableSet()
  private val structToShortName = mutableMapOf<Struct, String>()

  fun rememberFqn(jsonFqn: String, struct: Struct): String {
    val existingStructMapping = structToShortName[struct]
    if (existingStructMapping != null) {
      return existingStructMapping
    }

    val shortenedName = jsonFqn.substringAfterLast('.')

    val uniqueName =
      if (names.add(shortenedName)) {
        shortenedName
      }
      else {
        generateSequence(1, Int::inc)
          .map { index -> "$shortenedName$index" }
          .first(names::add)
      }

    structToShortName[struct] = uniqueName
    return uniqueName
  }

  fun getUniqueName(struct: Struct): String {
    return structToShortName[struct] ?: "Unknown"
  }
}

private sealed class PbPastedEntity {
  abstract fun render(): String

  data class PbStruct(
    val name: String,
    val fields: List<PbField>
  ) : PbPastedEntity() {
    override fun render(): String {
      return """
        message $name {
          ${fields.joinToString(separator = "\n") { it.render() }}
        }
      """.trimIndent()
    }
  }

  data class PbField(
    val name: String,
    val isRepeated: Boolean,
    val type: String,
    val order: Int
  ) : PbPastedEntity() {
    override fun render(): String {
      return """
        ${if (isRepeated) "repeated" else ""} $type $name = $order;
      """.trimIndent()
    }
  }
}

private const val DEFAULT_MESSAGE_NAME = "PastedObject"
private val PROTOBUF_JSON_DATA_FLAVOR = DataFlavor(PbJsonCopyPasteProcessor::class.java,
                                                   "JSON to Protobuf converter")