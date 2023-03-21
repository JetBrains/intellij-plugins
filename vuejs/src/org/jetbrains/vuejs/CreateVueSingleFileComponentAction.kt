// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.annotations.Nls
import org.jetbrains.vuejs.VueCreateFromTemplateHandler.Companion.VUE_RECENT_TEMPLATES
import org.jetbrains.vuejs.context.getVueClassComponentLibrary
import org.jetbrains.vuejs.context.hasVueFiles
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.context.supportsScriptSetup

class CreateVueSingleFileComponentAction : CreateFileFromTemplateAction(VueBundle.message("vue.create.single.file.component.action.text"),
                                                                        VueBundle.message(
                                                                          "vue.create.single.file.component.action.description"),
                                                                        VuejsIcons.Vue), DumbAware {
  companion object {

    @Nls
    private val name = VueBundle.message("vue.create.single.file.component.action.text")
  }

  override fun isAvailable(dataContext: DataContext): Boolean =
    super.isAvailable(dataContext)
    && (PROJECT.getData(dataContext)?.let { hasVueFiles(it) } == true
        || (PSI_ELEMENT.getData(dataContext) ?: PSI_FILE.getData(dataContext))?.let { isVueContext(it) } == true)

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(VueBundle.message("vue.create.single.file.component.action.dialog.title", name))

    val recentNames = PropertiesComponent.getInstance(project).getList(VUE_RECENT_TEMPLATES) ?: emptyList()

    listOfNotNull(
      VueCreateFromTemplateHandler.VUE_CLASS_API_TEMPLATE_NAME.takeIf { getVueClassComponentLibrary(directory) != null },
      VueCreateFromTemplateHandler.VUE_COMPOSITION_API_TEMPLATE_NAME.takeIf { supportsScriptSetup(directory) },
      VueCreateFromTemplateHandler.VUE_OPTIONS_API_TEMPLATE_NAME
    )
      .sortedByDescending { recentNames.indexOf(it) }
      .forEach { name ->
        builder.addKind(
          when (name) {
            VueCreateFromTemplateHandler.VUE_CLASS_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.class.api")
            VueCreateFromTemplateHandler.VUE_COMPOSITION_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.composition.api")
            VueCreateFromTemplateHandler.VUE_OPTIONS_API_TEMPLATE_NAME -> VueBundle.message(
              "vue.create.single.file.component.template.options.api")
            else -> throw IllegalStateException(name)
          },
          VuejsIcons.Vue,
          name
        )
      }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    VueBundle.message("vue.create.single.file.component.action.name", name, newName)

}
