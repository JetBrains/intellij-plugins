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
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.angular2.codeInsight.Angular2Processor.isTemplateTag;

public class Angular2MatchingComponentsInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(tag);
    List<Angular2Directive> components = ContainerUtil.filter(
      new Angular2ApplicableDirectivesProvider(tag).getMatched(), d -> d.isComponent() && scope.contains(d));
    final TextRange startTag = ObjectUtils.notNull(XmlTagUtil.getStartTagRange(tag), () -> tag.getTextRange())
      .shiftLeft(tag.getTextOffset());
    if (isTemplateTag(tag.getName())) {
      if (!components.isEmpty()) {
        holder.registerProblem(tag, startTag, "Components on an embedded template: " + renderDirectiveList(components));
      }
    }
    else {
      if (components.size() > 1) {
        holder.registerProblem(tag, startTag, "More than one component matched on this element: " + renderDirectiveList(components));
      }
    }
  }

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    if (descriptor.getInfo().type == Angular2AttributeType.TEMPLATE_BINDINGS
        && !isTemplateTag(attribute.getParent().getName())) {
      Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
      List<Angular2Directive> components = ContainerUtil.filter(
        new Angular2ApplicableDirectivesProvider(Angular2TemplateBindings.get(attribute)).getMatched(),
        d -> d.isComponent() && scope.contains(d));
      if (!components.isEmpty()) {
        holder.registerProblem(attribute, "Components on an embedded template: " + renderDirectiveList(components));
      }
    }
  }

  public static String renderDirectiveList(List<Angular2Directive> directives) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < directives.size(); i++) {
      if (i > 0) {
        if (i == directives.size() - 1) {
          result.append(" and ");
        }
        else {
          result.append(", ");
        }
      }
      Angular2Directive component = directives.get(i);
      result.append(component.getName());
      result.append(" (");
      result.append(component.getSelector().getText());
      result.append(')');
    }
    return result.toString();
  }
}
