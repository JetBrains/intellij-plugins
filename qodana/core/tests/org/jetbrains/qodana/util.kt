package org.jetbrains.qodana

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile

fun VirtualFile.getDocument(): Document? = FileDocumentManager.getInstance().getDocument(this)

fun notImplemented(): Nothing = throw NotImplementedError()