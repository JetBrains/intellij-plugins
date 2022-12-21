// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.inspections.quickfixes.ConvertToPropertyBindingQuickFix
import org.angular2.inspections.quickfixes.RemoveAttributeValueQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.psi.PropertyBindingType

class AngularInvalidAnimationTriggerAssignmentInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info as? PropertyBindingInfo
    if (info != null
        && info.bindingType == PropertyBindingType.ANIMATION
        && attribute.name.startsWith("@")
        && attribute.valueElement != null
        && !(attribute.value).isNullOrEmpty()) {
      holder.registerProblem(attribute.valueElement!!,
                             Angular2Bundle.message("angular.inspection.animation-trigger-assignment.message"),
                             ConvertToPropertyBindingQuickFix("@" + info.name),
                             RemoveAttributeValueQuickFix())
    }
  }
}
