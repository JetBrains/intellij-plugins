// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class Angular2BindingToEventInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2HtmlElementVisitor() {
      @Override
      public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
        final String propertyName = propertyBinding.getPropertyName();
        if (propertyName.startsWith("on")) {
          switch (propertyBinding.getBindingType()) {
            case ATTRIBUTE:
              holder.registerProblem(propertyBinding.getNameElement(),
                                     "Binding to event attribute '" + propertyName + "' is disallowed for security reasons.",
                                     new ConvertToEventQuickFix(propertyName.substring(2)));
              break;
            case PROPERTY:
              if (propertyBinding.getDescriptor() == null) {
                holder.registerProblem(propertyBinding.getNameElement(),
                                       "Binding to event property '" + propertyName +
                                       "' is disallowed for security reasons. If '" + propertyName +
                                       "' is a directive input, make sure the directive is imported by the current module.",
                                       new ConvertToEventQuickFix(propertyName.substring(2)));
              }
              break;
            default:
          }
        }
      }
    };
  }

  private static class ConvertToEventQuickFix implements LocalQuickFix {

    private final String myEventName;

    private ConvertToEventQuickFix(@NotNull String eventName) {
      myEventName = eventName;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
      return "Bind to event (" + myEventName + ")";
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
        attribute.setName("(" + myEventName + ")");
      }
    }
  }
}
