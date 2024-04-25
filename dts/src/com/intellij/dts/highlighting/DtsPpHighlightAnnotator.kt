package com.intellij.dts.highlighting

import com.intellij.dts.lang.DtsPpTokenTypes
import com.intellij.dts.pp.highlighting.PpHighlightAnnotator
import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.openapi.editor.colors.TextAttributesKey

class DtsPpHighlightAnnotator : PpHighlightAnnotator() {
  override val tokenTypes: PpTokenTypes = DtsPpTokenTypes

  override val inactiveAttribute: TextAttributesKey = DtsTextAttributes.INACTIVE.attribute
}