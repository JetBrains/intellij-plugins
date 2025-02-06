// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.lang.Angular2LangUtil

abstract class AngularHtmlLikeTemplateLocalInspectionTool : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return if (holder.file is HtmlCompatibleFile && Angular2LangUtil.isAngular2Context(holder.file)) {
      doBuildVisitor(holder)
    }
    else PsiElementVisitor.EMPTY_VISITOR
  }

  private fun doBuildVisitor(holder: ProblemsHolder): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlAttribute(attribute: XmlAttribute) {
        val descriptor = attribute.descriptor as? Angular2AttributeDescriptor
        if (descriptor != null) {
          JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(attribute) {
            visitAngularAttribute(holder, attribute, descriptor)
          }
        }
      }

      override fun visitXmlTag(tag: XmlTag) {
        JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(tag) {
          this@AngularHtmlLikeTemplateLocalInspectionTool.visitXmlTag(holder, tag)
        }
      }
    }
  }

  protected open fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {}

  protected open fun visitAngularAttribute(holder: ProblemsHolder,
                                           attribute: XmlAttribute,
                                           descriptor: Angular2AttributeDescriptor) {
  }
}
