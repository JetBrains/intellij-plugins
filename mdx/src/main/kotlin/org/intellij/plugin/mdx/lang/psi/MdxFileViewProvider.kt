package org.intellij.plugin.mdx.lang.psi

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager


class MdxFileViewProvider(manager: PsiManager, virtualFile: VirtualFile, eventSystemEnabled: Boolean)
    : MultiplePsiFilesPerDocumentFileViewProvider(manager, virtualFile, eventSystemEnabled), TemplateLanguageFileViewProvider {

    private val myRelevantLanguages = mutableSetOf(baseLanguage, templateDataLanguage)

    override fun createFile(lang: Language): PsiFile? {
        if (lang === MdxLanguage) {
            return super.createFile(lang)
        }

        val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang) ?: return null

        val psiFile = parserDefinition.createFile(this)
        if (lang === templateDataLanguage && psiFile is PsiFileImpl) {
            psiFile.contentElementType = MdxTemplateDataElementType
        }

        return psiFile
    }

    override fun getBaseLanguage(): Language = MdxLanguage

    override fun getLanguages(): Set<Language> = myRelevantLanguages

    override fun getTemplateDataLanguage(): Language = MdxJSLanguage.INSTANCE


    override fun cloneInner(fileCopy: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider =
            MdxFileViewProvider(manager, fileCopy, false)

    override fun findElementAt(offset: Int, lang: Class<out Language?>): PsiElement? {
        if (lang == XMLLanguage::class.java) {
            val psi = getPsi(MdxJSLanguage.INSTANCE)
            if (psi != null && DialectDetector.isJSX(psi)) {
                val element = findElementAt(offset)
                if (element == null) {
                 return element
                }
                if (isXmlElement(element)) {
                    return element
                }
            }
        }
        return super.findElementAt(offset, lang)
    }

    private fun isXmlElement(element: PsiElement?): Boolean {
        if (element == null) return false
        if (element.language is XMLLanguage) return true
        return element is PsiWhiteSpace && element.getParent() is XmlTag
    }
}

class MdxFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(file: VirtualFile,
                                        language: Language,
                                        manager: PsiManager,
                                        eventSystemEnabled: Boolean): FileViewProvider =
            MdxFileViewProvider(manager, file, eventSystemEnabled)
}