// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILeafElementType
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueLangModeMarkerElementType(val langMode: LangMode) : IElementType("VUE_LANG_MODE_$langMode", VueLanguage.INSTANCE),
                                                             ILeafElementType {
    override fun createLeafNode(leafText: CharSequence): ASTNode {
      return LeafPsiElement(this, leafText)
    }
  }