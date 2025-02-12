// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutors
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.OuterLanguageElementType
import com.intellij.util.asSafely
import org.intellij.terraform.hil.psi.TerraformTemplateLanguage
import org.intellij.terraform.hil.psi.TerraformTemplateTokenTypes

internal class TftplFileViewProvider(psiManager: PsiManager,
                                     virtualFile: VirtualFile,
                                     eventSystemEnabled: Boolean,
                                     private val givenDataLanguage: Language = doComputeTemplateDataLanguage(virtualFile, psiManager.project)
) : MultiplePsiFilesPerDocumentFileViewProvider(psiManager, virtualFile, eventSystemEnabled), ConfigurableTemplateLanguageFileViewProvider {

  override fun createFile(lang: Language): PsiFile? {
    val dataLanguageParser = LanguageParserDefinitions.INSTANCE.forLanguage(lang) ?: return null

    return when {
      lang === baseLanguage -> {
        dataLanguageParser.createFile(this)
      }
      lang === templateDataLanguage -> {
        val file = dataLanguageParser.createFile(this) as PsiFileImpl
        file.contentElementType = TEMPLATE_DATA
        file
      }
      else -> {
        null
      }
    }
  }

  override fun getContentElementType(language: Language): IElementType? {
    return if (language === templateDataLanguage) TEMPLATE_DATA else null
  }

  override fun getBaseLanguage(): Language = TerraformTemplateLanguage

  override fun getLanguages(): Set<Language> = hashSetOf(baseLanguage, templateDataLanguage)

  override fun cloneInner(fileCopy: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider {
    return TftplFileViewProvider(manager, fileCopy, false, doComputeTemplateDataLanguage(virtualFile, manager.project))
  }

  override fun getTemplateDataLanguage(): Language {
    return givenDataLanguage
  }
}

// Note that the language MUST be computed for a physical virtual file, not a copy -
// otherwise a mapping from the TemplateDataLanguageMappings would not be used resulting in inability to parse a file!
internal fun doComputeTemplateDataLanguage(virtualFile: VirtualFile, project: Project): Language {
  val dataLanguage = TemplateDataLanguageMappings.getInstance(project)?.getMapping(virtualFile)
                     ?: tryGuessLanguageByCompositeExtension(virtualFile.name)
                     ?: PlainTextLanguage.INSTANCE
  val substituteLang = LanguageSubstitutors.getInstance()
    .substituteLanguage(dataLanguage, virtualFile, project)
  return if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang)) {
    substituteLang
  }
  else {
    dataLanguage
  }
}

private fun tryGuessLanguageByCompositeExtension(fileName: String): Language? {
  val (maybeDataLanguageExtension, maybeTemplateLanguageExtension) =
    fileName.split('.')
      .takeIf { it.size == 3 }
      ?.takeLast(2)
      ?.let { it.first() to it.last() } ?: return null

  if (maybeTemplateLanguageExtension !in knownTemplateExtensions) return null
  return getLanguageByExtension(maybeDataLanguageExtension)
}

internal fun getLanguageByExtension(maybeDataLanguageExtension: String): Language? {
  return FileTypeManager.getInstance()
    .getFileTypeByExtension(maybeDataLanguageExtension)
    .asSafely<LanguageFileType>()
    ?.language
}

internal val knownTemplateExtensions: Set<String> = setOf("tftpl", "tfpl")

private val TEMPLATE_FRAGMENT: IElementType = OuterLanguageElementType("TerraformTemplateSegmentElementType",
                                                                       TerraformTemplateLanguage)
private val TEMPLATE_DATA: IElementType = TemplateDataElementType("TerraformTemplateTextElementType",
                                                                  TerraformTemplateLanguage,
                                                                  TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED,
                                                                  TEMPLATE_FRAGMENT)