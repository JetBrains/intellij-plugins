package com.jetbrains.lang.dart.ide.toolingDaemon

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class DartActiveLocationChangeHandler(private val dtdService: DartToolingDaemonService, private var cs: CoroutineScope) {
  private var activeLocationNullSent: Boolean = false

  internal fun sendActiveLocationChangeEvent() {
    cs.launch {
      doSendActiveLocationChangeEvent()
    }
  }

  private suspend fun doSendActiveLocationChangeEvent() {
    if (!dtdService.webSocketReady) return

    val paramsObject = readAction { calcActiveLocationChangeParams() }
    val activeLocationNull = paramsObject.getAsJsonObject("eventData")?.getAsJsonObject("textDocument") == null
    if (!activeLocationNull || !activeLocationNullSent) {
      activeLocationNullSent = activeLocationNull
      dtdService.sendRequest("postEvent", paramsObject, false) { }
    }
  }

  @RequiresReadLock
  private fun calcActiveLocationChangeParams(): JsonObject {
    val paramsObject = JsonObject()
    paramsObject.addProperty("streamId", "Editor")
    paramsObject.addProperty("eventKind", "activeLocationChanged")

    val eventDataObject = JsonObject()
    paramsObject.add("eventData", eventDataObject)

    val selectionsArray = JsonArray()
    eventDataObject.add("selections", selectionsArray)

    val editor = FileEditorManager.getInstance(dtdService.project).selectedTextEditor
    val document = editor?.document
    val uri = editor?.virtualFile?.takeIf { it.extension == "dart" }?.let { dtdService.getFileUri(it) }

    if (uri != null) {
      val textDocumentObject = JsonObject()
      textDocumentObject.addProperty("uri", uri)
      eventDataObject.add("textDocument", textDocumentObject)

      if (document != null) {
        for (caret in editor.caretModel.allCarets) {
          val selectionStart = caret.selectionStart
          val selectionEnd = caret.selectionEnd
          val anchorOffset = if (caret.offset == selectionStart) selectionEnd else selectionStart
          val activeOffset = if (caret.offset == selectionStart) selectionStart else selectionEnd
          val (anchorLine, anchorColumn) = getLineAndColumn(document, anchorOffset)
          val (activeLine, activeColumn) = getLineAndColumn(document, activeOffset)

          val selectionObject = JsonObject()

          selectionObject.add("anchor", JsonObject().apply {
            addProperty("line", anchorLine)
            addProperty("character", anchorColumn)
          })

          selectionObject.add("active", JsonObject().apply {
            addProperty("line", activeLine)
            addProperty("character", activeColumn)
          })

          selectionsArray.add(selectionObject)
        }
      }
    }

    return paramsObject
  }

  private fun getLineAndColumn(document: Document, offset: Int): Pair<Int, Int> {
    val lineNumber = document.getLineNumber(offset)
    return lineNumber to offset - document.getLineStartOffset(lineNumber)
  }
}
