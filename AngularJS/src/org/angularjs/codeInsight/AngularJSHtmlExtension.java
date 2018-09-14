// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.HtmlXmlExtension;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.angular2.lang.html.psi.impl.Angular2HtmlBananaBoxBindingImpl;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSHtmlExtension extends HtmlXmlExtension {
  @Override
  public boolean isAvailable(PsiFile file) {
    return (super.isAvailable(file) && AngularIndexUtil.hasAngularJS(file.getProject()))
           || (file.getFileType() == Angular2HtmlFileType.INSTANCE && Angular2LangUtil.isAngular2Context(file));
  }

  @Override
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    for (XmlAttribute attribute : tag.getAttributes()) {
      if (("ng-" + attrName).equals(DirectiveUtil.normalizeAttributeName(attribute.getName()))) {
        return true;
      }
    }
    Ref<Boolean> result = new Ref<>();
    tag.acceptChildren(new Angular2HtmlElementVisitor() {
      @Override
      public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
        checkBinding(propertyBinding.getBindingType(), propertyBinding.getPropertyName());
      }

      @Override
      public void visitBananaBoxBinding(Angular2HtmlBananaBoxBindingImpl bananaBoxBinding) {
        checkBinding(bananaBoxBinding.getBindingType(), bananaBoxBinding.getPropertyName());
      }

      private void checkBinding(PropertyBindingType type,
                                String name) {
        switch (type) {
          case PROPERTY:
          case ATTRIBUTE:
            if (attrName.equals(name)) {
              result.set(Boolean.TRUE);
            }
          default:
        }
      }
    });
    if (!result.isNull()) {
      return result.get();
    }
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName);
  }


  @Override
  public SchemaPrefix getPrefixDeclaration(XmlTag context, String namespacePrefix) {
    if ("ng".equals(namespacePrefix)) {
      SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix, 0);
      if (attribute != null) return attribute;
    }
    if (namespacePrefix != null && (namespacePrefix.startsWith("(") || namespacePrefix.startsWith("["))) {
      SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix, 1);
      if (attribute != null) return attribute;
    }
    return super.getPrefixDeclaration(context, namespacePrefix);
  }

  @Nullable
  private static SchemaPrefix findAttributeSchema(XmlTag context, String namespacePrefix, int offset) {
    for (XmlAttribute attribute : context.getAttributes()) {
      if (attribute.getName().startsWith(namespacePrefix)) {
        return new SchemaPrefix(attribute, TextRange.create(offset, namespacePrefix.length()), namespacePrefix.substring(offset));
      }
    }
    return null;
  }
}
