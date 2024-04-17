package com.jetbrains.lang.dart.ide

import com.intellij.codeInsight.actions.ReaderModeMatcher
import com.intellij.codeInsight.actions.ReaderModeProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.lang.dart.DartFileType

class DartReaderModeMatcher : ReaderModeMatcher {
  override fun matches(project: Project, file: VirtualFile, editor: Editor?, mode: ReaderModeProvider.ReaderMode): Boolean? =
    if (FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) false else null
}
