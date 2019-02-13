// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2ComponentTemplatePropertyInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if (COMPONENT_DEC.equals(decorator.getDecoratorName())
            && Angular2LangUtil.isAngular2Context(decorator)) {
          JSObjectLiteralExpression initializer = getObjectLiteralInitializer(decorator);
          if (initializer == null) {
            return;
          }
          JSProperty templateUrl = initializer.findProperty(TEMPLATE_URL_PROP);
          JSProperty template = initializer.findProperty(TEMPLATE_PROP);
          if (template == null && templateUrl == null) {
            holder.registerProblem(initializer,
                                   Angular2Bundle.message("angular.inspection.decorator.missing-template",
                                                          Angular2EntityUtils.getEntityClassName(decorator)),
                                   new AddJSPropertyQuickFix(initializer, TEMPLATE_PROP, "\n\n", 1, true),
                                   new AddJSPropertyQuickFix(initializer, TEMPLATE_URL_PROP, "./", 2, false));
          }
          else if (template != null && templateUrl != null) {
            ContainerUtil.packNullables(template.getNameIdentifier(), templateUrl.getNameIdentifier())
              .forEach(id -> holder.registerProblem(id, Angular2Bundle.message("angular.inspection.decorator.duplicated-template"),
                                                    new RemoveJSProperty(StringUtil.unquoteString(id.getText()))));
          }
        }
      }
    };
  }


  private static class RemoveJSProperty implements LocalQuickFix {
    private final String myPropertyName;

    private RemoveJSProperty(@NotNull String name) {
      myPropertyName = name;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
      return Angular2Bundle.message("angular.quickfix.decorator.remove-property.name", myPropertyName);
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
      return Angular2Bundle.message("angular.quickfix.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final JSProperty property = ObjectUtils.tryCast(descriptor.getPsiElement().getParent(), JSProperty.class);
      if (property != null) {
        PsiElement parent = property.getParent();
        property.delete();
        FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat();
      }
    }
  }
}
