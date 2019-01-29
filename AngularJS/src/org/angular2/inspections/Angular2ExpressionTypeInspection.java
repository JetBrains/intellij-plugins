// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2ExpressionTypeInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new Angular2ElementVisitor() {
      @Override
      public void visitAngular2Binding(Angular2Binding binding) {
        XmlAttribute attribute = getParentOfType(binding, XmlAttribute.class);
        if (attribute == null) {
          attribute = getParentOfType(InjectedLanguageManager.getInstance(binding.getProject()).getInjectionHost(binding),
                                      XmlAttribute.class);
        }
        Angular2AttributeDescriptor descriptor =
          tryCast(doIfNotNull(attribute, XmlAttribute::getDescriptor), Angular2AttributeDescriptor.class);
        if (descriptor != null
            && (descriptor.getInfo().type == Angular2AttributeType.PROPERTY_BINDING
                || descriptor.getInfo().type == Angular2AttributeType.BANANA_BOX_BINDING)) {
          validateType(descriptor, binding);
        }
      }

      private void validateType(@Nullable Angular2AttributeDescriptor attributeDescriptor, @Nullable Angular2Binding binding) {
        if (attributeDescriptor == null || binding == null) {
          return;
        }
        JSType expectedType = attributeDescriptor.getJSType();
        JSExpression expression = binding.getExpression();
        if (expectedType == null || expression == null) {
          return;
        }
        JSType actualType = JSResolveUtil.getExpressionJSType(expression);
        if (actualType != null
            && !expectedType.isDirectlyAssignableType(actualType, JSTypeComparingContextService.getProcessingContextWithCache(binding))) {
          holder.registerProblem(binding, "Type '" + actualType.getTypeText() + "' is not assignable to type '"
                                          + expectedType.getTypeText() + "'");
        }
      }
    };
  }
}
