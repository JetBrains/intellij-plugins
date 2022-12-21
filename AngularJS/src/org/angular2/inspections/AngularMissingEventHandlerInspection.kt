// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.inspections.quickfixes.AddAttributeValueQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeType

class AngularMissingEventHandlerInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    if (descriptor.info.type == Angular2AttributeType.EVENT) {
      val value = attribute.valueElement
      if (value == null || value.textLength == 0) {
        holder.registerProblem(attribute,
                               Angular2Bundle.message("angular.inspection.missing-event-handler.message"),
                               AddAttributeValueQuickFix())
      }
      else if (value.value.trim { it <= ' ' }.isEmpty()) {
        holder.registerProblem(value, Angular2Bundle.message("angular.inspection.missing-event-handler.message"))
      }
    }
  }
}
