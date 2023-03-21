// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplate.ATTRIBUTE_NAME
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.html.VueFileType

class VueCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {

  companion object {
    const val VUE_CLASS_API_TEMPLATE_NAME: String = "Vue Class API Component"
    const val VUE_COMPOSITION_API_TEMPLATE_NAME: String = "Vue Composition API Component"
    const val VUE_OPTIONS_API_TEMPLATE_NAME: String = "Vue Options API Component"
    const val VUE_RECENT_TEMPLATES: String = "vue.recent.templates"
  }

  override fun handlesTemplate(template: FileTemplate): Boolean {
    val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.extension)
    return VueFileType.INSTANCE == fileType && template.name
      .let { it == VUE_CLASS_API_TEMPLATE_NAME || it == VUE_COMPOSITION_API_TEMPLATE_NAME || it == VUE_OPTIONS_API_TEMPLATE_NAME }
  }

  override fun isNameRequired(): Boolean = true

  override fun prepareProperties(props: MutableMap<String, Any>, filename: String?, template: FileTemplate, project: Project) {
    val name = props[ATTRIBUTE_NAME] as? String ?: return
    when (template.name) {
      VUE_OPTIONS_API_TEMPLATE_NAME -> {
        props["COMPONENT_NAME"] = name
      }
      VUE_COMPOSITION_API_TEMPLATE_NAME -> {
      }
      VUE_CLASS_API_TEMPLATE_NAME -> {
        props["COMPONENT_NAME"] = String(name.mapIndexed { index, c ->
          if ((index == 0 && !StringUtil.isJavaIdentifierStart(c))
              || (index == 1 && !StringUtil.isJavaIdentifierPart(c)))
            '_'
          else
            c
        }.toCharArray())
      }
    }
  }

  override fun createFromTemplate(project: Project,
                                  directory: PsiDirectory,
                                  fileName: String?,
                                  template: FileTemplate,
                                  templateText: String,
                                  props: MutableMap<String, Any>): PsiElement {
    val propertiesComponent = PropertiesComponent.getInstance(project)
    (propertiesComponent.getList(VUE_RECENT_TEMPLATES) ?: emptyList())
      .toMutableList()
      .let {
        it.remove(template.name)
        it.add(template.name)
        propertiesComponent.setList(VUE_RECENT_TEMPLATES, it.toList())
      }
    return super.createFromTemplate(project, directory, fileName, template, templateText, props)
  }
}
