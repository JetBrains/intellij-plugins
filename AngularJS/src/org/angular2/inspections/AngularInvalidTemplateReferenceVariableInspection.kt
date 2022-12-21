// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AngularInvalidTemplateReferenceVariableInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

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
          List<LocalQuickFix> quickFixes = new SmartList<>();
          Angular2DeclarationsScope.DeclarationProximity proximity = scope.getDeclarationsProximity(allMatching);
          if (proximity != Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE) {
            Angular2FixesFactory.addUnresolvedDeclarationFixes(attribute, quickFixes);
          }
          quickFixes.add(new RemoveAttributeIntentionFix(attribute.getName()));
          holder.registerProblem(attribute.getNameElement(),
                                 Angular2Bundle.message("angular.inspection.invalid-template-ref-var.message.unbound", exportName),
                                 Angular2InspectionUtils.getBaseProblemHighlightType(scope),
                                 range,
                                 quickFixes.toArray(LocalQuickFix.EMPTY_ARRAY));
        }
        else if (matching.size() > 1) {
          holder.registerProblem(attribute.getNameElement(),
                                 range,
                                 Angular2Bundle.message("angular.inspection.invalid-template-ref-var.message.ambiguous-name", exportName,
                                                        Angular2EntityUtils.renderEntityList(matching)));
        }
      }
    }
  }
}
