package org.jetbrains.vuejs

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import icons.VuejsIcons

/**
 * @author Vladislav.Sabenin on 08/8/2018.
 */
class CreateVueTypescriptSingleFileComponentAction : CreateFileFromTemplateAction(VueBundle.message("vue.create.typescript.single.file.component.action.text"),
                                                                        VueBundle.message("vue.create.typescript.single.file.component.action.description"),
                                                                        VuejsIcons.Vue), DumbAware {
  companion object {
    val VUE_TEMPLATE_NAME: String = "Vue Typescript Single File Component"
    private val name = VueBundle.message("vue.create.typescript.single.file.component.action.text")
  }

  override fun buildDialog(project: Project?, directory: PsiDirectory?, builder: CreateFileFromTemplateDialog.Builder?) {
    builder!!
      .setTitle("New $name")
      .addKind(name, JavaScriptSupportLoader.JAVASCRIPT.icon, VUE_TEMPLATE_NAME)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String?, templateName: String?): String = "Create $name $newName"
}
