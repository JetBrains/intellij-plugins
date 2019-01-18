// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class Angular2MultipleTemplateBindingsInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2HtmlElementVisitor() {
      @Override
      public void visitTemplateBindings(Angular2HtmlTemplateBindings bindings) {
        if (bindings.getParent() != null
            && ContainerUtil.find(bindings.getParent().getAttributes(),
                                  attr -> attr != bindings && attr instanceof Angular2HtmlTemplateBindings) != null) {
          holder
            .registerProblem(bindings.getNameElement(), "Only one structural directive (attribute prefixed with *) per element is allowed.",
                             new RemoveAttributeQuickFix(bindings.getName()));
        }
      }
    };
  }

  private static class RemoveAttributeQuickFix implements LocalQuickFix {
    private final String myAttributeName;

    private RemoveAttributeQuickFix(@NotNull String name) {myAttributeName = name;}

    @Nls
    @NotNull
    @Override
    public String getName() {
      return "Remove '" + myAttributeName + "' attribute";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "Angular";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final XmlAttribute attribute = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), XmlAttribute.class);
      if (attribute != null) {
        PsiElement parent = attribute.getParent();
        attribute.delete();
        FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat();
      }
    }
  }
}
