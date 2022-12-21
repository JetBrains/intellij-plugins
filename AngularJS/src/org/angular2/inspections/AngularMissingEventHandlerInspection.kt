// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.inspections.quickfixes.AddAttributeValueQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

public class AngularMissingEventHandlerInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    if (descriptor.getInfo().type == Angular2AttributeType.EVENT) {
      XmlAttributeValue value = attribute.getValueElement();
      if (value == null || value.getTextLength() == 0) {
        holder.registerProblem(attribute,
                               Angular2Bundle.message("angular.inspection.missing-event-handler.message"),
                               new AddAttributeValueQuickFix());
      }
      else if (value.getValue().trim().isEmpty()) {
        holder.registerProblem(value, Angular2Bundle.message("angular.inspection.missing-event-handler.message"));
      }
    }
  }
}
