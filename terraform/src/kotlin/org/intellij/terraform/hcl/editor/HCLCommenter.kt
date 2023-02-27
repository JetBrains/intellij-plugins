// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.application.options.CodeStyle
import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import org.intellij.terraform.hcl.HCLFileType
import org.intellij.terraform.hcl.formatter.HCLCodeStyleSettings
import org.intellij.terraform.hcl.formatter.HCLCodeStyleSettings.LineCommenterPrefix
import org.intellij.terraform.config.TerraformFileType

class HCLCommenter : MultipleLangCommentProvider, Commenter {
  companion object {
    private val COMMENTER_KEY = Key<MyCommenter>("Project.HCLCommenter")
  }

  override fun canProcess(file: PsiFile?, viewProvider: FileViewProvider?) =
      file?.fileType in setOf(HCLFileType, TerraformFileType)

  override fun getLineCommenter(file: PsiFile?, editor: Editor?, lineStartLanguage: Language?, lineEndLanguage: Language?): Commenter {
    val project = file?.project ?: return this
    val settings = CodeStyle.getCustomSettings(file, HCLCodeStyleSettings::class.java)
    var commenter = project.getUserData(COMMENTER_KEY)
    if (commenter != null && settings == commenter.settings) return commenter
    commenter = MyCommenter(settings)
    project.putUserData(COMMENTER_KEY, commenter)
    return commenter
  }

  override fun getLineCommentPrefix() = LineCommenterPrefix.LINE_DOUBLE_SLASHES.prefix

  override fun getBlockCommentPrefix() = "/*"

  override fun getBlockCommentSuffix() = "*/"

  override fun getCommentedBlockCommentPrefix(): String? = null

  override fun getCommentedBlockCommentSuffix(): String? = null

  class MyCommenter(val settings: HCLCodeStyleSettings) : Commenter {
    override fun getLineCommentPrefix() = (
        LineCommenterPrefix.values().find { it.id == settings.PROPERTY_LINE_COMMENTER_CHARACTER }
            ?: LineCommenterPrefix.LINE_DOUBLE_SLASHES
        ).prefix

    override fun getBlockCommentPrefix() = "/*"

    override fun getBlockCommentSuffix() = "*/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
  } 
}
