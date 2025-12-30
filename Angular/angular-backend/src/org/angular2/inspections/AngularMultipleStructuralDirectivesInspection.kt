// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeType

class AngularMultipleStructuralDirectivesInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {
    val templateBindings = tag.attributes.filter { attr ->
      val descriptor = attr.descriptor as? Angular2AttributeDescriptor
      descriptor != null && descriptor.info.type == Angular2AttributeType.TEMPLATE_BINDINGS
    }
    if (templateBindings.size > 1) {
      templateBindings.forEach { attr ->
        holder.registerProblem(
          attr.nameElement,
          Angular2Bundle.message("angular.inspection.multiple-structural-directives.message"),
          RemoveAttributeIntentionFix(attr.name))
      }
    }
  }
}
