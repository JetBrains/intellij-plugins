// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.frontmatter

import com.intellij.lang.javascript.dialects.TypeScriptParserDefinition
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType

class AstroFrontmatterParserDefinition: TypeScriptParserDefinition() {
  private val AFM_FILE = JSFileElementType.create(AstroFrontmatterLanguage.INSTANCE)

  override fun getFileNodeType(): IFileElementType =
    AFM_FILE

}