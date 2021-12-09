// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.parser.Angular2HtmlVarAttrTokenType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlLet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2HtmlLetImpl extends Angular2HtmlBoundAttributeImpl implements Angular2HtmlLet {

  public Angular2HtmlLetImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public XmlElement getNameElement() {
    XmlElement res = super.getNameElement();
    if (res == null &&
        getFirstChild().getNode().getElementType() == Angular2HtmlVarAttrTokenType.LET) {
      return (XmlElement)getFirstChild();
    }
    return res;
  }

  @Override
  public @Nullable JSVariable getVariable() {
    return PsiTreeUtil.findChildOfType(this, JSVariable.class);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitLet(this);
    }
    else if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public @NotNull String getVariableName() {
    return getAttributeInfo().name;
  }
}
