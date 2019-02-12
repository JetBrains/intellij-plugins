// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.inspections.quickfixes.RemoveAttributeQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.EVENT_ATTR_PREFIX;

public class Angular2BindingToEventInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.PROPERTY_BINDING) {
      final String propertyName = info.name;
      if (propertyName.startsWith(EVENT_ATTR_PREFIX)) {
        switch (((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType) {
          case ATTRIBUTE:
            holder.registerProblem(attribute.getNameElement(),
                                   Angular2Bundle.message("angular.inspection.template.binding-to-event-attribute", propertyName),
                                   new ConvertToEventQuickFix(propertyName.substring(2)),
                                   new RemoveAttributeQuickFix(attribute.getName()));
            break;
          case PROPERTY:
            Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
            if (descriptor.getSourceDirectives() == null
                || ContainerUtil.find(descriptor.getSourceDirectives(), scope::contains) == null) {
              holder.registerProblem(attribute.getNameElement(),
                                     Angular2Bundle.message("angular.inspection.template.binding-to-event-property", propertyName),
                                     new ConvertToEventQuickFix(propertyName.substring(2)),
                                     new RemoveAttributeQuickFix(attribute.getName()));
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
      return Angular2Bundle.message("angular.quickfix.template.bind-to-event.name", myEventName);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return Angular2Bundle.message("angular.quickfix.family");
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
