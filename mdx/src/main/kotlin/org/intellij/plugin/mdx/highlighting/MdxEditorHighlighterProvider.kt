package org.intellij.plugin.mdx.highlighting

import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class MdxEditorHighlighterProvider : EditorHighlighterProvider {

    override fun getEditorHighlighter(project: Project?,
                                      fileType: FileType,
                                      virtualFile: VirtualFile?,
                                      colors: EditorColorsScheme): EditorHighlighter = MdxEditorHighlighter(colors)
}