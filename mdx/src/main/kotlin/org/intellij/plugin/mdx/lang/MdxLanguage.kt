package org.intellij.plugin.mdx.lang

import com.intellij.lang.xml.XMLLanguage
import com.intellij.psi.templateLanguages.TemplateLanguage
import org.intellij.plugins.markdown.lang.MarkdownLanguage

object MdxLanguage : XMLLanguage(MarkdownLanguage.INSTANCE,"MDX"), TemplateLanguage
