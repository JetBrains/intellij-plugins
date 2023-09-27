package com.intellij.protobuf.lang.refactoring

import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
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
    val namesScope = collectExistingNames(pbFile, caretOffset) ?: return
    val protobufStructure = tryBuildStructFromJson(data.maybeJson, namesScope) ?: return
    val document = editor.document
    PsiDocumentManager.getInstance(project).commitDocument(document)

    ApplicationManager.getApplication().runWriteAction {
      document.replaceString(bounds.startOffset, bounds.endOffset, protobufStructure)
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

  private fun tryBuildStructFromJson(jsonContent: String, namesScope: List<String>): String? {
    return runCatching {
      val dynamicStruct = Struct.newBuilder()
        .apply {
          JsonFormat.parser().merge(jsonContent, this)
        }.build()

      renderStruct(null, PbNameSuggester(namesScope), dynamicStruct)
    }.onFailure {
      thisLogger().warn("Error during JSON to Protobuf conversion", it)
    }.getOrNull()
  }

  private fun renderStruct(nameCandidate: String?, nameSuggester: PbNameSuggester, struct: Struct): String {
    return buildString {
      append("message", " ")
      append(nameSuggester.uniqueNameFor(nameCandidate))
      append(" ", "{", "\n")

      val nestedNameSuggester = PbNameSuggester(struct.allFields.map { it.key.name })
      struct.fieldsMap.entries.forEachIndexed { index, (key, value) ->
        if (value.hasStructValue()) {
          append(renderStruct(key, nestedNameSuggester, value.structValue))
        }
        else {
          append(renderField(name = key, value = value, index = index + 1))
        }
      }
      append("}", "\n")
    }
  }

  private fun renderField(name: String, value: Value, index: Int): String {
    return buildString {
      append(getTypeQualifiers(value).joinToString(separator = " ", postfix = " "))
      append(name)
      append(" = $index;")
      append("\n")
    }
  }

  private fun getTypeQualifiers(value: Value): List<String> {
    return buildList {
      if (value.hasListValue()) add("repeated")
      val pbType = when {
        value.hasBoolValue() -> "bool"
        value.hasStringValue() -> "string"
        value.hasNumberValue() -> "uint32"
        else -> "string"
      }
      add(pbType)
    }
  }
}

private class PbJsonTransferableData(val maybeJson: String) : TextBlockTransferableData {
  override fun getFlavor(): DataFlavor {
    return PROTOBUF_JSON_DATA_FLAVOR
  }
}

private class PbNameSuggester(existingNames: Collection<String>) {
  private val names = existingNames.toMutableSet()

  fun uniqueNameFor(nameCandidate: String?): String {
    val effectiveNameCandidate = nameCandidate ?: DEFAULT_MESSAGE_NAME
    val capitalizedNameCandidate = StringUtil.capitalize(effectiveNameCandidate)
    return if (names.add(capitalizedNameCandidate)) {
      capitalizedNameCandidate
    }
    else {
      generateSequence(1, Int::inc)
        .map { index -> "$capitalizedNameCandidate$index" }
        .first(names::add)
    }
  }
}

private const val DEFAULT_MESSAGE_NAME = "PastedObject"
private val PROTOBUF_JSON_DATA_FLAVOR = DataFlavor(PbJsonCopyPasteProcessor::class.java,
                                                   "JSON to Protobuf converter")