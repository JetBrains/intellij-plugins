// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
      props.put("COMPONENT_NAME", name)
    }
  }
}