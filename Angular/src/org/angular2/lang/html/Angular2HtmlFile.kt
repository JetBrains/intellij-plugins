// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lang.javascript.config.JSConfig
import com.intellij.lang.javascript.config.JSConfigProvider
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType
import org.angular2.entities.source.Angular2SourceUtil

class Angular2HtmlFile(viewProvider: FileViewProvider, fileElementType: IFileElementType)
  : HtmlFileImpl(viewProvider, fileElementType), JSControlFlowScope, JSConfigProvider {

  override fun isTopmostControlFlowScope() = true

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getJSConfig(): JSConfig? {
    val importGraphIncludedFile = Angular2SourceUtil.findComponentClass(this)?.containingFile ?: return null
    return TypeScriptConfigUtil.getConfigForPsiFile(importGraphIncludedFile)
  }
}
