// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType.XML_NAME
import org.angular2.Angular2InjectionUtils.getElementAtCaretFromContext
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.entities.Angular2Directive

object Angular2EditorUtils {

  internal fun getDirectivesAtCaret(context: DataContext): List<Angular2Directive> {
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
    return directives
  }
}
