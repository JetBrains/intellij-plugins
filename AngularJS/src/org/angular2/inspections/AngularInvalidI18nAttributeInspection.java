// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

public class AngularInvalidI18nAttributeInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    Angular2AttributeNameParser.AttributeInfo info = descriptor.getInfo();
    XmlTag parent = attribute.getParent();
    if (info.type == Angular2AttributeType.I18N && parent != null) {
      TextRange range = attribute.getNameElement().getTextRangeInParent();
      String i18nedAttrName = info.name;
      if (i18nedAttrName.isEmpty()) {
        holder.registerProblem(attribute, range,
                               Angular2Bundle.message("angular.inspection.template.i18n.empty"));
      } else if (descriptor.getDeclarations().isEmpty()) {
        holder.registerProblem(attribute, new TextRange(range.getStartOffset() + 5, range.getEndOffset()),
                               Angular2Bundle.message("angular.inspection.template.i18n.not-matching", i18nedAttrName));
      }
    }
  }
}
