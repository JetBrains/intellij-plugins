// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.html.HtmlLikeFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

public abstract class AngularHtmlLikeTemplateLocalInspectionTool extends LocalInspectionTool {

  @Override
  public final @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    if (holder.getFile() instanceof HtmlLikeFile
        && Angular2LangUtil.isAngular2Context(holder.getFile())) {
      return doBuildVisitor(holder);
    }
    return PsiElementVisitor.EMPTY_VISITOR;
  }

  protected @NotNull PsiElementVisitor doBuildVisitor(@NotNull ProblemsHolder holder) {
    return new XmlElementVisitor() {
      @Override
      public void visitXmlAttribute(XmlAttribute attribute) {
        Angular2AttributeDescriptor descriptor = ObjectUtils.tryCast(attribute.getDescriptor(), Angular2AttributeDescriptor.class);
        if (descriptor != null) {
          visitAngularAttribute(holder, attribute, descriptor);
        }
      }

      @Override
      public void visitXmlTag(XmlTag tag) {
        AngularHtmlLikeTemplateLocalInspectionTool.this.visitXmlTag(holder, tag);
      }
    };
  }

  protected void visitXmlTag(@NotNull ProblemsHolder holder, @NotNull XmlTag tag) {
  }

  protected void visitAngularAttribute(@NotNull ProblemsHolder holder,
                                       @NotNull XmlAttribute attribute,
                                       @NotNull Angular2AttributeDescriptor descriptor) {
  }
}
