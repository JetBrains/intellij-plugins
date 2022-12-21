// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.inspections.quickfixes.ConvertToPropertyBindingQuickFix;
import org.angular2.inspections.quickfixes.RemoveAttributeValueQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;

public class AngularInvalidAnimationTriggerAssignmentInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    PropertyBindingInfo info = ObjectUtils.tryCast(descriptor.getInfo(), PropertyBindingInfo.class);
    if (info != null
        && info.bindingType == PropertyBindingType.ANIMATION
        && attribute.getName().startsWith("@")
        && attribute.getValueElement() != null
        && !StringUtil.notNullize(attribute.getValue()).isEmpty()) {
      holder.registerProblem(attribute.getValueElement(),
                             Angular2Bundle.message("angular.inspection.animation-trigger-assignment.message"),
                             new ConvertToPropertyBindingQuickFix("@" + info.name),
                             new RemoveAttributeValueQuickFix());
    }
  }
}
