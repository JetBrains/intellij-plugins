// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.inspections.quickfixes.ConvertToEventQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.EVENT_ATTR_PREFIX;

public class AngularInsecureBindingToEventInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.PROPERTY_BINDING) {
      final String propertyName = info.name;
      if (propertyName.startsWith(EVENT_ATTR_PREFIX)) {
        switch (((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType) {
          case ATTRIBUTE -> holder.registerProblem(attribute.getNameElement(),
                                                   Angular2Bundle.message("angular.inspection.insecure-binding-to-event.message.attribute",
                                                                          propertyName),
                                                   new ConvertToEventQuickFix(propertyName.substring(2)),
                                                   new RemoveAttributeIntentionFix(attribute.getName()));
          case PROPERTY -> {
            Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
            if (ContainerUtil.find(descriptor.getSourceDirectives(), scope::contains) == null) {
              holder.registerProblem(attribute.getNameElement(),
                                     Angular2Bundle.message("angular.inspection.insecure-binding-to-event.message.property", propertyName),
                                     new ConvertToEventQuickFix(propertyName.substring(2)),
                                     new RemoveAttributeIntentionFix(attribute.getName()));
            }
          }
          default -> { }
        }
      }
    }
  }
}
