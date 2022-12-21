// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.angular2.inspections.quickfixes.RemoveTagContentQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ELEMENT_NG_CONTENT

class AngularNonEmptyNgContentInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {
    if (ELEMENT_NG_CONTENT == tag.name) {
      val content = tag.value.children
      if (content.any { el -> el !is XmlText || !el.getText().trim { it <= ' ' }.isEmpty() }) {
        @Suppress("DialogTitleCapitalization")
        holder.registerProblem(tag, TextRange(content[0].textRangeInParent.startOffset,
                                              content[content.size - 1].textRangeInParent.endOffset),
                               Angular2Bundle.message("angular.inspection.ng-content-with-content.message"),
                               RemoveTagContentQuickFix()
        )
      }
    }
  }
}
