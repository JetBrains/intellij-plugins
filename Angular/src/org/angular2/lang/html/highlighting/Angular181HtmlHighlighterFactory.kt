// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.lang.html.Angular2TemplateSyntax

class Angular181HtmlHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    //TODO take interpolation setup into account
    val interpolationConfig: Pair<String, String>? = null
    return Angular2HtmlFileHighlighter(Angular2TemplateSyntax.V_18_1, interpolationConfig)
  }
}