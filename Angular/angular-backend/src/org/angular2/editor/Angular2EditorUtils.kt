// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.psi.util.runWithTimeout
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType.XML_NAME
import org.angular2.Angular2InjectionUtils.getElementAtCaretFromContext
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.entities.Angular2Directive
import org.angular2.lang.Angular2LangUtil

object Angular2EditorUtils {

  internal fun getDirectivesAtCaret(context: DataContext): List<Angular2Directive> {
    val file = context.getData(CommonDataKeys.PSI_FILE) ?: return emptyList()
    if (!file.fileType
        .let {
          it == TypeScriptFileType
          || Angular2LangUtil.isAngular2HtmlFileType(it)
          || Angular2LangUtil.isAngular2SvgFileType(it)
        }
        || !Angular2LangUtil.isAngular2Context(file))
      return emptyList()
    return runWithTimeout(100) {
      var element = getElementAtCaretFromContext(context)
      var directives = emptyList<Angular2Directive>()
      if (element != null && element.node.elementType === XML_NAME) {
        element = element.parent
      }
      if (element is XmlAttribute) {
        val descriptor = element.descriptor as? Angular2AttributeDescriptor
        if (descriptor != null) {
          directives = descriptor.sourceDirectives
        }
      }
      else if (element is XmlTag) {
        val descriptor = element.descriptor as? Angular2ElementDescriptor
        if (descriptor != null) {
          directives = descriptor.sourceDirectives
        }
      }
      if (directives.isNotEmpty() && element != null)
        directives.filter(Angular2DeclarationsScope(element)::contains)
      else
        directives
    } ?: emptyList()
  }
}
