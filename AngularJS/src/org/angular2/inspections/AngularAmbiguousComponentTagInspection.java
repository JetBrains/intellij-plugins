// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.XmlTagUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;

public class AngularAmbiguousComponentTagInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(tag);
    if (scope.getModule() == null) {
      return;
    }
    List<Angular2Directive> components = ContainerUtil.filter(
      new Angular2ApplicableDirectivesProvider(tag).getMatched(), d -> d.isComponent() && scope.contains(d));
    final TextRange startTag = ObjectUtils.notNull(XmlTagUtil.getStartTagRange(tag), () -> tag.getTextRange())
      .shiftLeft(tag.getTextOffset());
    if (isTemplateTag(tag)) {
      if (!components.isEmpty()) {
        holder.registerProblem(tag, startTag, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.embedded",
          Angular2EntityUtils.renderEntityList(components)));
      }
    }
    else {
      if (components.size() > 1) {
        holder.registerProblem(tag, startTag, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.many-components",
          Angular2EntityUtils.renderEntityList(components)));
      }
    }
  }

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    if (descriptor.getInfo().type == Angular2AttributeType.TEMPLATE_BINDINGS
        && !isTemplateTag(attribute.getParent())) {
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
      List<Angular2Directive> components = ContainerUtil.filter(
        new Angular2ApplicableDirectivesProvider(Angular2TemplateBindings.get(attribute)).getMatched(),
        d -> d.isComponent() && scope.contains(d));
      if (!components.isEmpty()) {
        holder.registerProblem(attribute, Angular2Bundle.message(
          "angular.inspection.ambiguous-component-tag.message.embedded",
          Angular2EntityUtils.renderEntityList(components)));
      }
    }
  }
}
