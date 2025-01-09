// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.lang.javascript.config.JSConfig
import com.intellij.lang.javascript.config.JSConfigProvider
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.tree.IFileElementType
import org.angular2.entities.source.Angular2SourceUtil

class Angular2HtmlFile(viewProvider: FileViewProvider, fileElementType: IFileElementType)
  : HtmlFileImpl(viewProvider, fileElementType), JSControlFlowScope, JSConfigProvider {

  override fun isTopmostControlFlowScope(): Boolean = true

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getJSConfig(): JSConfig? {
    val importGraphIncludedFile = Angular2SourceUtil.findComponentClass(this)?.containingFile
    if (importGraphIncludedFile == null) {
      if (ApplicationManager.getApplication().isUnitTestMode && !NodeModuleUtil.hasNodeModulesInPath(this)) {
        return TypeScriptConfigUtil.getParentConfigWithName(virtualFile, TypeScriptConfig.TS_CONFIG_JSON)
          ?.let { TypeScriptConfigService.Provider.get(project).parseConfigFile(it) }
      }
      return null
    }
    return TypeScriptConfigUtil.getConfigForPsiFile(importGraphIncludedFile)
  }
}
