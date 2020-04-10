// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.BlockEx
import com.intellij.formatting.Indent
import com.intellij.lang.Language
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.formatter.xml.XmlFormattingPolicy

class VueSyntheticBlock(subBlocks: List<Block>,
                        parent: Block,
                        indent: Indent?,
                        policy: XmlFormattingPolicy,
                        childIndent: Indent?,
                        private val myLanguage: Language?)
  : SyntheticBlock(subBlocks, parent, indent, policy, childIndent), BlockEx {
  override fun getLanguage(): Language? = myLanguage
}
