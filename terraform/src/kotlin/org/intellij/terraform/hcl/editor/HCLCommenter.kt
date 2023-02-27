/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
