// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.html.impl.providers.HtmlAttributeValueProvider;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2AttributeValueProvider extends HtmlAttributeValueProvider {

  public static boolean isNgClassAttribute(@Nullable XmlAttribute attribute) {
    return attribute instanceof Angular2HtmlPropertyBinding
           && ((Angular2HtmlPropertyBinding)attribute).getBindingType() == PropertyBindingType.PROPERTY
           && "ngClass".equals(((Angular2HtmlPropertyBinding)attribute).getPropertyName());
  }

  @Nullable
  @Override
  public String getCustomAttributeValues(XmlTag tag, String attributeName) {
    XmlAttribute attribute = tag.getAttribute(attributeName);
    if (isNgClassAttribute(attribute)) {
      return getClassNames(((Angular2HtmlPropertyBinding)attribute).getBinding());
    }
    return null;
  }

  @NotNull
  private static String getClassNames(@Nullable Angular2Binding binding) {
    StringBuilder result = new StringBuilder();
    if (binding != null && binding.getExpression() != null) {
      binding.getExpression().accept(new JSElementVisitor() {
        @Override
        public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
          for (JSExpression expression : node.getExpressions()) {
            expression.accept(this);
          }
        }

        @Override
        public void visitJSLiteralExpression(JSLiteralExpression node) {
          if (node.isQuotedLiteral()) {
            result.append(node.getStringValue());
            result.append(' ');
          }
        }

        @Override
        public void visitJSObjectLiteralExpression(JSObjectLiteralExpression node) {
          for (JSProperty property : node.getProperties()) {
            property.accept(this);
          }
        }

        @Override
        public void visitJSProperty(JSProperty node) {
          result.append(node.getName());
          result.append(' ');
        }
      });
    }
    return result.toString();
  }
}
