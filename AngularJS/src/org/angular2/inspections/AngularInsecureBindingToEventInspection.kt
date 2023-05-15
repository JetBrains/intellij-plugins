// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.inspections.quickfixes.ConvertToEventQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.PropertyBindingType.ATTRIBUTE
import org.angular2.lang.html.psi.PropertyBindingType.PROPERTY
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.EVENT_ATTR_PREFIX

class AngularInsecureBindingToEventInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info
    if (info.type == Angular2AttributeType.PROPERTY_BINDING) {
      val propertyName = info.name
      if (propertyName.startsWith(EVENT_ATTR_PREFIX)) {
        when ((info as Angular2AttributeNameParser.PropertyBindingInfo).bindingType) {
          ATTRIBUTE -> holder.registerProblem(attribute.nameElement,
                                              Angular2Bundle.message("angular.inspection.insecure-binding-to-event.message.attribute",
                                                                     propertyName),
                                              ConvertToEventQuickFix(propertyName.substring(2)),
                                              RemoveAttributeIntentionFix(attribute.name))
          PROPERTY -> {
            val scope = Angular2DeclarationsScope(attribute)
            if (descriptor.sourceDirectives.find { scope.contains(it) } == null) {
              holder.registerProblem(attribute.nameElement,
                                     Angular2Bundle.message("angular.inspection.insecure-binding-to-event.message.property", propertyName),
                                     ConvertToEventQuickFix(propertyName.substring(2)),
                                     RemoveAttributeIntentionFix(attribute.name))
            }
          }
          else -> {}
        }
      }
    }
  }
}
