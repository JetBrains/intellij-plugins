package com.intellij.dts.core.highlighting

import com.intellij.dts.core.lang.DtsPpTokenTypes
import com.intellij.dts.pp.highlighting.PpHighlightAnnotator
import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.openapi.editor.colors.TextAttributesKey

class DtsPpHighlightAnnotator : PpHighlightAnnotator() {
  override val tokenTypes: PpTokenTypes = DtsPpTokenTypes

  override val inactiveAttribute: TextAttributesKey = DtsTextAttributes.INACTIVE.attribute
}