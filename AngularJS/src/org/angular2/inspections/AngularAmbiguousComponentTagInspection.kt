// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ObjectUtils
import com.intellij.xml.util.XmlTagUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2EntityUtils
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeType

class AngularAmbiguousComponentTagInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {
    val scope = Angular2DeclarationsScope(tag)
    if (scope.importsOwner == null) {
      return
    }
    val components = Angular2ApplicableDirectivesProvider(tag).matched
      .filter { d -> d.isComponent && scope.contains(d) }
    val startTag = ObjectUtils.notNull(XmlTagUtil.getStartTagRange(tag)) { tag.textRange }
      .shiftLeft(tag.textOffset)
    if (isTemplateTag(tag)) {
      if (!components.isEmpty()) {
        holder.registerProblem(tag, startTag, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.embedded",
          Angular2EntityUtils.renderEntityList(components)))
      }
    }
    else {
      if (components.size > 1) {
        holder.registerProblem(tag, startTag, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.many-components",
          Angular2EntityUtils.renderEntityList(components)))
      }
    }
  }

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    if (descriptor.info.type == Angular2AttributeType.TEMPLATE_BINDINGS && !isTemplateTag(attribute.parent)) {
      val scope = Angular2DeclarationsScope(attribute)
      val components = Angular2ApplicableDirectivesProvider(Angular2TemplateBindings.get(attribute)).matched
        .filter { d -> d.isComponent && scope.contains(d) }
      if (!components.isEmpty()) {
        holder.registerProblem(attribute, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.embedded",
          Angular2EntityUtils.renderEntityList(components)))
      }
    }
  }
}
