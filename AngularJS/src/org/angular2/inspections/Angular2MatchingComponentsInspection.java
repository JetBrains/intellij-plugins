// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.util.XmlTagUtil;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.entities.Angular2Directive;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2MatchingComponentsInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new XmlElementVisitor() {

      @Override
      public void visitXmlTag(XmlTag tag) {
        List<Angular2Directive> components = ContainerUtil.filter(new Angular2ApplicableDirectivesProvider(tag).getMatched(),
                                                                  Angular2Directive::isComponent);
        final TextRange startTag = ObjectUtils.notNull(XmlTagUtil.getStartTagRange(tag), () -> tag.getTextRange())
          .shiftLeft(tag.getTextOffset());
        if (Angular2Processor.isTemplateTag(tag.getName())) {
          if (!components.isEmpty()) {
            holder.registerProblem(tag, startTag, "Components on an embedded template: " + renderComponentList(components));
          }
        }
        else {
          if (components.size() > 1) {
            holder.registerProblem(tag, startTag, "More than one component matched on this element: " + renderComponentList(components));
          }
        }
      }

      private String renderComponentList(List<Angular2Directive> components) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < components.size(); i++) {
          if (i > 0) {
            if (i == components.size() - 1) {
              result.append(" and ");
            }
            else {
              result.append(" , ");
            }
          }
          Angular2Directive component = components.get(i);
          result.append(component.getName());
          result.append(" (");
          result.append(component.getSelector().getText());
          result.append(')');
        }
        return result.toString();
      }
    };
  }
}
