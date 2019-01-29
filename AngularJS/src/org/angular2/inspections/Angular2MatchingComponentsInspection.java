// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.XmlTagUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.entities.Angular2Directive;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2MatchingComponentsInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(tag);
    List<Angular2Directive> components = ContainerUtil.filter(
      new Angular2ApplicableDirectivesProvider(tag).getMatched(), d -> d.isComponent() && scope.contains(d));
    final TextRange startTag = ObjectUtils.notNull(XmlTagUtil.getStartTagRange(tag), () -> tag.getTextRange())
      .shiftLeft(tag.getTextOffset());
    if (Angular2Processor.isTemplateTag(tag.getName())) {
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

  public static String renderDirectiveList(List<Angular2Directive> directives) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < directives.size(); i++) {
      if (i > 0) {
        if (i == directives.size() - 1) {
          result.append(" and ");
        }
        else {
          result.append(" , ");
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
