package org.jetbrains.vuejs

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplate.ATTRIBUTE_NAME
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import org.jetbrains.vuejs.CreateVueSingleFileComponentAction.Companion.VUE_TEMPLATE_NAME

/**
 * @author Irina.Chernushina on 10/5/2017.
 */
class VueCreateFromTemplateHandler : DefaultCreateFromTemplateHandler() {
  override fun handlesTemplate(template: FileTemplate?): Boolean {
    template ?: return false
    val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.extension)
    return VueFileType.INSTANCE == fileType && VUE_TEMPLATE_NAME == template.name
  }

  override fun isNameRequired(): Boolean = true

  override fun prepareProperties(props: MutableMap<String, Any>?) {
    if (props != null) {
      val name = props[ATTRIBUTE_NAME] as? String ?: return
      props.put("KEBAB_CASE_NAME", org.jetbrains.vuejs.codeInsight.fromAsset(name))
    }
  }
}