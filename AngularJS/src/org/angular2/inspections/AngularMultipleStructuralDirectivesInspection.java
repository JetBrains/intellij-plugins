// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.util.ObjectUtils.tryCast;

public class AngularMultipleStructuralDirectivesInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    List<XmlAttribute> templateBindings = ContainerUtil.filter(tag.getAttributes(), attr -> {
      Angular2AttributeDescriptor descriptor = tryCast(attr.getDescriptor(), Angular2AttributeDescriptor.class);
      return descriptor != null && descriptor.getInfo().type == Angular2AttributeType.TEMPLATE_BINDINGS;
    });
    if (templateBindings.size() > 1) {
      templateBindings.forEach(attr -> holder.registerProblem(
        attr.getNameElement(),
        Angular2Bundle.message("angular.inspection.multiple-structural-directives.message"),
        new RemoveAttributeIntentionFix(attr.getName())));
    }
  }
}
