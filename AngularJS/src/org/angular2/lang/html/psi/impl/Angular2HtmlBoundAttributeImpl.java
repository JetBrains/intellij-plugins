// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.xml.XmlElement;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.xml.XmlTokenType.XML_NAME;

public class Angular2HtmlBoundAttributeImpl extends XmlAttributeImpl implements Angular2HtmlBoundAttribute {

  public Angular2HtmlBoundAttributeImpl(@NotNull Angular2ElementType elementType) {
    super(elementType);
  }

  @Override
  public XmlElement getNameElement() {
    XmlElement result = super.getNameElement();
    if (result == null && getFirstChild() instanceof PsiErrorElement
      && getFirstChild().getFirstChild().getNode().getElementType() == XML_NAME) {
      return (XmlElement)getFirstChild().getFirstChild();
    }
    return result;
  }


}
