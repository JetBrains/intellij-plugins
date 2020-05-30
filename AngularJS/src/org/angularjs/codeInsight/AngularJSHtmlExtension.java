// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.HtmlXmlExtension;
import org.angularjs.codeInsight.tags.AngularJSTagDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AngularJSHtmlExtension extends HtmlXmlExtension {
  @Override
  public boolean isAvailable(PsiFile file) {
    return super.isAvailable(file) && AngularIndexUtil.hasAngularJS(file.getProject());
  }

  @Override
  public boolean isSelfClosingTagAllowed(@NotNull XmlTag tag) {
    return tag.getDescriptor() instanceof AngularJSTagDescriptor
           || super.isSelfClosingTagAllowed(tag);
  }

  @Override
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    String ngAttr = DirectiveUtil.normalizeAttributeName("ng-" + attrName);
    for (XmlAttribute attribute : tag.getAttributes()) {
      if (ngAttr.equals(DirectiveUtil.normalizeAttributeName(attribute.getName()))) {
        return true;
      }
    }
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName);
  }


  @Override
  public SchemaPrefix getPrefixDeclaration(XmlTag context, String namespacePrefix) {
    if ("ng".equals(namespacePrefix)) {
      SchemaPrefix attribute = findAttributeSchema(context, namespacePrefix);
      if (attribute != null) return attribute;
    }
    return super.getPrefixDeclaration(context, namespacePrefix);
  }

  private static @Nullable SchemaPrefix findAttributeSchema(XmlTag context, String namespacePrefix) {
    for (XmlAttribute attribute : context.getAttributes()) {
      if (attribute.getName().startsWith(namespacePrefix)) {
        return new SchemaPrefix(attribute, TextRange.create(0, namespacePrefix.length()), namespacePrefix);
      }
    }
    return null;
  }
}
