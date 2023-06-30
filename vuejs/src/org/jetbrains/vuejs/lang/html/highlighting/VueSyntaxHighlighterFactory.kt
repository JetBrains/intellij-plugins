// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.highlighting

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.html.VueFileElementType
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile

class VueSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    val langMode = VueScriptLangs.getLatestKnownLang(project, virtualFile)
    return getSyntaxHighlighter(project, virtualFile, langMode)
  }

  companion object {
    fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?, langMode: LangMode): SyntaxHighlighter =
      VueFileHighlighter(
        project?.let { JSRootConfiguration.getInstance(it).languageLevel } ?: JSLanguageLevel.getLevelForJSX(),
        langMode,
        project,
        VueFileElementType.readDelimiters(virtualFile?.name),
        virtualFile?.isDotVueFile == false
      )
  }

}
