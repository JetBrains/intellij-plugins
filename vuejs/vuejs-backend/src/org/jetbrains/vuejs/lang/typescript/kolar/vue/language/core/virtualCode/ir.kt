// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.findTopLevelVueTag
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl.RootNodeImpl
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IR
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRContent
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScript
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptSetup
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRTemplate

fun useIR(
  file: VueFile,
): IR {
  return IR(
    template = getTemplate(file),
    script = getScript(file),
    scriptSetup = getScriptSetup(file),
    styles = emptyList(),
  )
}

private fun getTemplate(
  file: VueFile,
): IRTemplate? {
  val templateTag = findTopLevelVueTag(file, TEMPLATE_TAG_NAME)
                    ?: return null

  return IRTemplate(
    name = Source("template"),
    lang = "html",
    content = IRContentImpl(templateTag),
    ast = RootNodeImpl(templateTag),
  )
}

private fun getScript(
  file: VueFile,
): IRScript? {
  val embeddedContent = findScriptTag(file, setup = false)
                          ?.embeddedContent
                        ?: return null

  return IRScript(
    name = Source("script"),
    lang = lang(file),
    content = IRContentImpl(embeddedContent),
    src = null, // TBD
    ast = embeddedContent,
  )
}

private fun getScriptSetup(
  file: VueFile,
): IRScriptSetup? {
  val embeddedContent = findScriptTag(file, setup = false)
                          ?.embeddedContent
                        ?: return null

  return IRScriptSetup(
    name = Source("scriptSetup"),
    lang = lang(file),
    content = IRContentImpl(embeddedContent),
    generic = null, // TBD
    ast = embeddedContent,
  )
}

private fun lang(file: VueFile): String =
  file.langMode.canonicalAttrValue

private val XmlTag.embeddedContent: JSEmbeddedContent?
  get() = PsiTreeUtil.getStubChildOfType(this, JSEmbeddedContent::class.java)

private class IRContentImpl(
  private val element: PsiElement,
) : IRContent {
  override val startOffset: Int
    get() = element.startOffset

  override val endOffset: Int
    get() = element.endOffset

  override fun substring(
    startIndex: Int,
    endIndex: Int,
  ): String {
    require(startIndex >= startOffset) {
      "startIndex $startIndex must be >= startOffset $startOffset"
    }
    require(endIndex <= endOffset) {
      "endIndex $endIndex must be <= endOffset $endOffset"
    }

    return element.text.substring(
      startIndex - startOffset,
      endIndex - startOffset,
    )
  }
}
