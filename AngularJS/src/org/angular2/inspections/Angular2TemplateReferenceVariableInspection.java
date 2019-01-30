// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.inspections.quickfixes.RemoveAttributeQuickFix;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2TemplateReferenceVariableInspection extends Angular2HtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
    AttributeInfo info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.REFERENCE) {
      String exportName = attribute.getValue();
      if (exportName != null && !exportName.isEmpty()) {
        List<Angular2Directive> allMatching =
          ContainerUtil.filter(new Angular2ApplicableDirectivesProvider(attribute.getParent()).getMatched(),
                               dir -> dir.getExportAsList().contains(exportName));
        Angular2DeclarationsScope scope = new Angular2DeclarationsScope(attribute);
        List<Angular2Directive> matching = ContainerUtil.filter(allMatching, d -> scope.contains(d));
        TextRange range = new TextRange(0, info.name.length()).shiftRight(attribute.getName().length() - info.name.length());
        if (matching.isEmpty()) {
          // TODO Provide quick fixes for module import
          holder.registerProblem(attribute.getNameElement(),
                                 "There is no directive with 'exportAs' set to '" + exportName + "'",
                                 scope.isFullyResolved()
                                 ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                 : ProblemHighlightType.WEAK_WARNING,
                                 range,
                                 new RemoveAttributeQuickFix(attribute.getName()));
        }
        else if (matching.size() > 1) {
          holder.registerProblem(attribute.getNameElement(),
                                 range,
                                 "There are multiple directives with 'exportAs' set to '" + exportName + "': "
                                 + Angular2MatchingComponentsInspection.renderDirectiveList(matching));
        }
      }
    }
  }
}
