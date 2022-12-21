// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.SmartList;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.XmlTagUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.tags.Angular2ElementDescriptor;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE;

public class AngularUndefinedTagInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    XmlElementDescriptor descriptor = tag.getDescriptor();
    if (!(descriptor instanceof Angular2ElementDescriptor)
        || ((Angular2ElementDescriptor)descriptor).isImplied()) {
      return;
    }
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(tag);
    Angular2ApplicableDirectivesProvider provider = new Angular2ApplicableDirectivesProvider(tag, true);
    DeclarationProximity proximity = scope.getDeclarationsProximity(provider.getMatched());
    if (proximity == IN_SCOPE) {
      return;
    }
    XmlToken tagName = XmlTagUtil.getStartTagNameElement(tag);
    if (tagName == null) {
      return;
    }
    List<LocalQuickFix> quickFixes = new SmartList<>();
    if (proximity != NOT_REACHABLE) {
      Angular2FixesFactory.addUnresolvedDeclarationFixes(tag, quickFixes);
    }
    holder.registerProblem(tagName,
                           Angular2Bundle.message("angular.inspection.undefined-tag.message.out-of-scope", tagName.getText()),
                           Angular2InspectionUtils.getBaseProblemHighlightType(scope),
                           quickFixes.toArray(LocalQuickFix.EMPTY_ARRAY));
  }
}
