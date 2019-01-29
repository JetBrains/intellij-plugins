// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class Angular2BindingToEventInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.PROPERTY_BINDING) {
      final String propertyName = info.name;
      if (propertyName.startsWith("on")) {
        switch (((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType) {
          case ATTRIBUTE:
            holder.registerProblem(attribute.getNameElement(),
                                   "Binding to event attribute '" + propertyName + "' is disallowed for security reasons.",
                                   new ConvertToEventQuickFix(propertyName.substring(2)));
            break;
          case PROPERTY:
            if (descriptor.getSourceDirectives() == null
                || descriptor.getSourceDirectives().length == 0) {
              holder.registerProblem(attribute.getNameElement(),
                                     "Binding to event property '" + propertyName +
                                     "' is disallowed for security reasons. If '" + propertyName +
                                     "' is a directive input, make sure the directive is imported by the current module.",
                                     new ConvertToEventQuickFix(propertyName.substring(2)));
            }
            break;
          default:
        }
      }
    }
  }

  private static class ConvertToEventQuickFix implements LocalQuickFix {

    private final String myEventName;

    private ConvertToEventQuickFix(@NotNull String eventName) {
      myEventName = eventName;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
      return "Bind to event (" + myEventName + ")";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "Angular";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), XmlAttribute.class);
      if (attribute != null) {
        attribute.setName("(" + myEventName + ")");
      }
    }
  }
}
