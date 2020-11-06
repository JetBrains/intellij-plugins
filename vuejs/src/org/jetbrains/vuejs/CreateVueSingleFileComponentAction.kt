// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.annotations.Nls
import org.jetbrains.vuejs.context.hasVueFiles
import org.jetbrains.vuejs.context.isVueContext

class CreateVueSingleFileComponentAction : CreateFileFromTemplateAction(VueBundle.message("vue.create.single.file.component.action.text"),
                                                                        VueBundle.message(
                                                                          "vue.create.single.file.component.action.description"),
                                                                        VuejsIcons.Vue), DumbAware {
  companion object {
    const val VUE_TEMPLATE_NAME: String = "Vue Single File Component"

    @Nls
    private val name = VueBundle.message("vue.create.single.file.component.action.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean {
    return super.isAvailable(dataContext)
           && (PROJECT.getData(dataContext)?.let { hasVueFiles(it) } == true
               || (PSI_ELEMENT.getData(dataContext) ?: PSI_FILE.getData(dataContext))?.let { isVueContext(it) } == true)
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(VueBundle.message("vue.create.single.file.component.action.dialog.title", name))
      .addKind(name, VuejsIcons.Vue, VUE_TEMPLATE_NAME)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    VueBundle.message("vue.create.single.file.component.action.name", name, newName)

}
