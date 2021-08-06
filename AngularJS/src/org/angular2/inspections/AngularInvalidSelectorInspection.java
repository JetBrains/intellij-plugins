// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.html.HtmlCompatibleFile;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.jetbrains.annotations.NotNull;

import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.codeInsight.tags.Angular2NgContentDescriptor.ATTR_SELECT;
import static org.angular2.codeInsight.tags.Angular2TagDescriptorsProvider.NG_CONTENT;

public class AngularInvalidSelectorInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    if (holder.getFile() instanceof HtmlCompatibleFile) {
      return new XmlElementVisitor() {
        @Override
        public void visitXmlAttribute(XmlAttribute attribute) {
          XmlAttributeValue value;
          if (attribute.getName().equals(ATTR_SELECT)
              && attribute.getParent().getName().equals(NG_CONTENT)
              && (value = attribute.getValueElement()) != null) {
            try {
              Angular2DirectiveSimpleSelector.parse(value.getValue());
            }
            catch (Angular2DirectiveSimpleSelector.ParseException e) {
              holder.registerProblem(value,
                                     e.getErrorRange().shiftRight(value.getText().indexOf(value.getValue())),
                                     e.getMessage());
            }
          }
        }
      };
    }
    else if (DialectDetector.isTypeScript(holder.getFile())
             && Angular2LangUtil.isAngular2Context(holder.getFile())) {
      return new JSElementVisitor() {

        @Override
        public void visitES6Decorator(ES6Decorator decorator) {
          if (isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC)) {
            JSObjectLiteralExpression initializer = getObjectLiteralInitializer(decorator);
            if (initializer == null) {
              return;
            }
            JSProperty selector = initializer.findProperty(SELECTOR_PROP);
            String text;
            if (selector == null) {
              if (DIRECTIVE_DEC.equals(decorator.getDecoratorName())) {
                holder.registerProblem(initializer,
                                       Angular2Bundle.message("angular.inspection.invalid-directive-selector.message.missing"),
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
    return PsiElementVisitor.EMPTY_VISITOR;
  }
}
