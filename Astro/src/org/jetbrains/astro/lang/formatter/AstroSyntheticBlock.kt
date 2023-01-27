// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.BlockEx
import com.intellij.formatting.Indent
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.formatter.xml.SyntheticBlock

class AstroSyntheticBlock(subBlocks: List<Block>,
                          parent: Block,
                          indent: Indent?,
                          policy: AstroFormattingPolicy,
                          childIndent: Indent?)
  : SyntheticBlock(subBlocks, parent, indent, policy, childIndent), BlockEx {

  override fun getLanguage(): Language? =
    HTMLLanguage.INSTANCE

}
