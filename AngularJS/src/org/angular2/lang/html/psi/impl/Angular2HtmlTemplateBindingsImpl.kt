// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.expr.psi.impl.Angular2EmptyTemplateBindings;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlTemplateBindingsImpl extends Angular2HtmlBoundAttributeImpl implements Angular2HtmlTemplateBindings {

  public Angular2HtmlTemplateBindingsImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitTemplateBindings(this);
    }
    else if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public @NotNull Angular2TemplateBindings getBindings() {
    return ObjectUtils.notNull(
      ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2TemplateBindings.class)),
      () -> new Angular2EmptyTemplateBindings(this, getTemplateName()));
  }

  @Override
  public @NotNull String getTemplateName() {
    return getAttributeInfo().name;
  }
}
