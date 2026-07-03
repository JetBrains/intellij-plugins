// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.findTopLevelVueTag
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
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
    ast = null, // TBD
  )
}

private fun getScript(
  file: VueFile,
): IRScript? {
  val scriptTag = findScriptTag(file, setup = false)
                  ?: return null

  return IRScript(
    name = Source("script"),
    lang = lang(file),
    content = IRContentImpl(scriptTag),
    src = null, // TBD
    ast = SourceFileImpl(scriptTag),
  )
}

private fun getScriptSetup(
  file: VueFile,
): IRScriptSetup? {
  val scriptTag = findScriptTag(file, setup = false)
                  ?: return null

  return IRScriptSetup(
    name = Source("scriptSetup"),
    lang = lang(file),
    content = IRContentImpl(scriptTag),
    generic = null, // TBD
    ast = SourceFileImpl(scriptTag),
  )
}

private fun lang(file: VueFile): String =
  file.langMode.canonicalAttrValue

private class IRContentImpl(
  element: PsiElement,
) : IRContent {
  override val length: Int
    get() = TODO("not implemented")

  override fun get(index: Int): Char {
    TODO("not implemented")
  }

  override fun indexOf(string: String, startIndex: Int): Int {
    TODO("not implemented")
  }

  override fun substring(startIndex: Int, endIndex: Int): String {
    TODO("not implemented")
  }
}

private fun SourceFileImpl(
  scriptTag: XmlTag,
): SourceFile =
  TODO("not implemented")
