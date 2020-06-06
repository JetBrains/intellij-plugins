// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import icons.VuejsIcons
import org.jetbrains.vuejs.context.isVueContext

class CreateVueSingleFileComponentAction : CreateFileFromTemplateAction(VueBundle.message("vue.create.single.file.component.action.text"),
                                                                        VueBundle.message(
                                                                          "vue.create.single.file.component.action.description"),
                                                                        VuejsIcons.Vue), DumbAware {
  companion object {
    const val VUE_TEMPLATE_NAME: String = "Vue Single File Component"
    private val name = VueBundle.message("vue.create.single.file.component.action.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean {
    return super.isAvailable(dataContext) && isVueContext(
      PlatformDataKeys.PSI_ELEMENT.getData(dataContext) ?: return false)
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle("New $name")
      .addKind(name, VuejsIcons.Vue, VUE_TEMPLATE_NAME)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = "Create $name $newName"
}
