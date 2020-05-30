// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix;
import org.angular2.inspections.quickfixes.RemoveJSProperty;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import static org.angular2.Angular2DecoratorUtil.*;

public class AngularIncorrectTemplateDefinitionInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if (isAngularEntityDecorator(decorator, COMPONENT_DEC)) {
          JSObjectLiteralExpression initializer = getObjectLiteralInitializer(decorator);
          if (initializer == null) {
            return;
          }
          JSProperty templateUrl = initializer.findProperty(TEMPLATE_URL_PROP);
          JSProperty template = initializer.findProperty(TEMPLATE_PROP);
          if (template == null && templateUrl == null) {
            Angular2Component component = Angular2EntitiesProvider.getComponent(decorator);
            if (component != null
                && component.getTypeScriptClass() != null
                && component.getTypeScriptClass().getName() != null) {
              holder.registerProblem(initializer,
                                     Angular2Bundle.message("angular.inspection.invalid-template-definition.message.missing",
                                                            component.getTypeScriptClass().getName()),
                                     new AddJSPropertyQuickFix(initializer, TEMPLATE_PROP, "\n\n", 1, true),
                                     new AddJSPropertyQuickFix(initializer, TEMPLATE_URL_PROP, "./", 2, false));
            }
          }
          else if (template != null && templateUrl != null) {
            //noinspection DialogTitleCapitalization
            ContainerUtil.packNullables(template.getNameIdentifier(), templateUrl.getNameIdentifier())
              .forEach(id -> holder.registerProblem(
                id, Angular2Bundle.message("angular.inspection.invalid-template-definition.message.duplicated"),
                new RemoveJSProperty(StringUtil.unquoteString(id.getText()))));
          }
        }
      }
    };
  }
}
