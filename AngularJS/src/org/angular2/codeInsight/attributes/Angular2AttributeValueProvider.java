// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.html.impl.providers.HtmlAttributeValueProvider;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.xml.util.HtmlUtil;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.angular2.lang.html.parser.Angular2AttributeNameParser.parse;
import static org.angular2.lang.html.psi.PropertyBindingType.CLASS;

public class Angular2AttributeValueProvider extends HtmlAttributeValueProvider {

  @NonNls public static final String NG_CLASS_ATTR = "ngClass";

  public static boolean isNgClassAttribute(@Nullable XmlAttribute attribute) {
    return attribute != null && (isNgClassAttribute(parse(attribute.getName(), attribute.getParent())));
  }

  public static boolean isNgClassAttribute(@NotNull AttributeInfo info) {
    return info instanceof PropertyBindingInfo
           && ((PropertyBindingInfo)info).bindingType == PropertyBindingType.PROPERTY
           && NG_CLASS_ATTR.equals(info.name);
  }

  @Override
  public @Nullable String getCustomAttributeValues(final XmlTag tag, final String attributeName) {
    if (Angular2LangUtil.isAngular2Context(tag)) {
      if (attributeName.equalsIgnoreCase(HtmlUtil.CLASS_ATTRIBUTE_NAME)) {
        List<String> result = new SmartList<>();
        String classAttr = null;
        for (XmlAttribute attribute : tag.getAttributes()) {
          String attrName = attribute.getName();
          if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equalsIgnoreCase(attrName)) {
            classAttr = attribute.getValue();
          }
          else {
            ObjectUtils.doIfNotNull(getCustomAttributeValues(tag, attrName), result::add);
          }
        }
        if (!result.isEmpty()) {
          ObjectUtils.doIfNotNull(classAttr, result::add);
          return StringUtil.join(result, " ");
        }
        return null;
      }
      else {
        AttributeInfo info = parse(attributeName, tag);
        if (isNgClassAttribute(info)) {
          XmlAttribute attribute = tag.getAttribute(attributeName);
          if (attribute instanceof Angular2HtmlPropertyBinding) {
            return getClassNames(((Angular2HtmlPropertyBinding)attribute).getBinding());
          }
        }
        else if (info instanceof PropertyBindingInfo
                 && ((PropertyBindingInfo)info).bindingType == CLASS) {
          return info.name;
        }
      }
    }
    return null;
  }

  private static @NotNull String getClassNames(@Nullable Angular2Binding binding) {
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
