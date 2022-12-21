// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.inspections.quickfixes.RemoveTagContentQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_CONTENT;

public class AngularNonEmptyNgContentInspection extends AngularHtmlLikeTemplateLocalInspectionTool {

  @Override
  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
    if (ELEMENT_NG_CONTENT.equals(tag.getName())) {
      XmlTagChild[] content = tag.getValue().getChildren();
      if (ContainerUtil.find(content, el -> !(el instanceof XmlText)
                                            || !el.getText().trim().isEmpty()) != null) {
        holder.registerProblem(tag, new TextRange(content[0].getTextRangeInParent().getStartOffset(),
                                                  content[content.length - 1].getTextRangeInParent().getEndOffset()),
                               Angular2Bundle.message("angular.inspection.ng-content-with-content.message"),
                               new RemoveTagContentQuickFix()
        );
      }
    }
  }
}
