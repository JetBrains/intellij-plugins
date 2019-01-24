// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2DirectiveSelectorInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if ((COMPONENT_DEC.equals(decorator.getDecoratorName())
             || DIRECTIVE_DEC.equals(decorator.getDecoratorName()))
            && Angular2LangUtil.isAngular2Context(decorator)) {
          JSObjectLiteralExpression initializer = getObjectLiteralInitializer(decorator);
          if (initializer == null) {
            return;
          }
          JSProperty selector = initializer.findProperty(SELECTOR_PROP);
          String text;
          if (selector == null) {
            if (DIRECTIVE_DEC.equals(decorator.getDecoratorName())) {
              holder.registerProblem(initializer, "Directive is missing required 'selector' property.",
                                     new AddJSPropertyQuickFix(initializer, SELECTOR_PROP, "", 0, false));
            }
            else {
              holder.registerProblem(decorator,
                                     "Component is missing 'selector' property. Assuming default 'ng-component' selector.",
                                     ProblemHighlightType.WEAK_WARNING,
                                     getDecoratorNameRange(decorator),
                                     new AddJSPropertyQuickFix(initializer, SELECTOR_PROP, "", 0, false));
            }
          }
          else if ((text = getExpressionStringValue(selector.getValue())) != null) {
            try {
              Angular2DirectiveSimpleSelector.parse(text);
            }
            catch (Angular2DirectiveSimpleSelector.ParseException e) {
              holder.registerProblem(selector.getValue(),
                                     e.getErrorRange().shiftRight(1), e.getMessage());
            }
          }
        }
      }
    };
  }

  private static TextRange getDecoratorNameRange(ES6Decorator decorator) {
    PsiElement location = doIfNotNull(tryCast(decorator.getExpression(), JSCallExpression.class),
                                      JSCallExpression::getMethodExpression);
    if (location != null) {
      return new TextRange(0, location.getTextRange().getEndOffset() - decorator.getTextOffset());
    }
    else {
      return new TextRange(0, decorator.getTextLength());
    }
  }
}
