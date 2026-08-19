package com.intellij.mdx.backend.highlighting

import com.intellij.codeInsight.daemon.impl.analysis.DefaultHighlightingSettingProvider
import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.plugin.mdx.lang.MdxFileType

/**
 * JS/TS injected into an MDX code fence is a documentation snippet, not a real module, so incomplete code
 * must not be flagged as an error. Mirrors `JSInMarkdownHighlightingSettingProvider`, which only
 * recognizes a `MarkdownFileType` host and so leaves MDX hosts at full highlighting.
 */
internal class MdxInjectedHighlightingSettingProvider : DefaultHighlightingSettingProvider() {
  override fun getDefaultSetting(project: Project, file: VirtualFile): FileHighlightingSetting? {
    if (file !is VirtualFileWindow) return null
    val fileType = file.fileType
    val isJavaScript = DialectDetector.JAVASCRIPT_FILE_TYPES.contains(fileType)
    val isTypeScript = !isJavaScript && TypeScriptUtil.TYPESCRIPT_FILE_TYPES.contains(fileType)
    if (!isJavaScript && !isTypeScript) return null
    val hostFileType = FileTypeRegistry.getInstance().getFileTypeByFileName(file.delegate.name)
    return if (hostFileType == MdxFileType) FileHighlightingSetting.SKIP_INSPECTION else null
  }
}
