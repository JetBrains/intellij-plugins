// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ArrayUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.types.Angular2PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class AngularInvalidExpressionResultTypeInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2ElementVisitor() {
      @Override
      public void visitAngular2Binding(Angular2Binding binding) {
        validateBinding(binding,
                        (b, attribute) -> pair(b.getExpression(), new Angular2PropertyBindingType(attribute)),
                        Angular2AttributeType.PROPERTY_BINDING,
                        Angular2AttributeType.BANANA_BOX_BINDING);
      }

      @Override
      public void visitAngular2TemplateBinding(Angular2TemplateBinding templateBinding) {
        validateBinding(templateBinding,
                        (binding, descriptor) -> pair(binding.getExpression(), binding.getKeyJSType()),
                        Angular2AttributeType.TEMPLATE_BINDINGS);
      }

      private <T extends JSElement> void validateBinding(@Nullable T binding,
                                                         @NotNull BiFunction<T, XmlAttribute, Pair<JSExpression, JSType>>
                                                           getTypeAndExpression,
                                                         Angular2AttributeType @NotNull ... supportedTypes) {
        if (binding == null) {
          return;
        }
        XmlAttribute attribute = getParentOfType(binding, XmlAttribute.class);
        if (attribute == null) {
          attribute = getParentOfType(InjectedLanguageManager.getInstance(binding.getProject()).getInjectionHost(binding),
                                      XmlAttribute.class);
        }
        Angular2AttributeDescriptor descriptor =
          tryCast(doIfNotNull(attribute, XmlAttribute::getDescriptor), Angular2AttributeDescriptor.class);
        if (descriptor != null
            && ArrayUtil.contains(descriptor.getInfo().type, supportedTypes)) {
          validateType(attribute, binding, getTypeAndExpression);
        }
      }

      private <T extends JSElement> void validateType(@NotNull XmlAttribute attribute,
                                                      @NotNull T binding,
                                                      @NotNull BiFunction<T, XmlAttribute, Pair<JSExpression, JSType>>
                                                        getTypeAndExpression) {
        Pair<JSExpression, JSType> typeAndExpression = getTypeAndExpression.apply(binding, attribute);
        JSType expectedType = typeAndExpression.second;
        JSExpression expression = typeAndExpression.first;
        if (expectedType == null || expression == null) {
          return;
        }
        JSType actualType = JSResolveUtil.getElementJSType(expression, true);
        if (actualType != null
            && !expectedType.isDirectlyAssignableType(actualType, JSTypeComparingContextService.getProcessingContextWithCache(binding))) {
          holder.registerProblem(expression, Angular2Bundle.message("angular.inspection.invalid-expr-result-type.message",
                                                                    actualType.getTypeText(JSType.TypeTextFormat.PRESENTABLE),
                                                                    expectedType.getTypeText(JSType.TypeTextFormat.PRESENTABLE)));
        }
      }
    };
  }
}
