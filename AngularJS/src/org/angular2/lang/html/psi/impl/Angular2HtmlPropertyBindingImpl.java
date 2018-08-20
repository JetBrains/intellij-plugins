// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2HtmlPropertyBindingImpl extends XmlAttributeImpl implements Angular2HtmlPropertyBinding {

  public Angular2HtmlPropertyBindingImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitPropertyBinding(this);
    }
    else if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @NotNull
  @Override
  public String getPropertyName() {
    String name = getName();
    if (name.startsWith("[") && name.endsWith("]")) {
      return name.substring(1, name.length() - 1);
    }
    else if (name.startsWith("bind-")) {
      return name.substring(5);
    }
    throw new IllegalStateException("Bad attribute name: " + name);
  }

  @Nullable
  @Override
  public Angular2Binding getBinding() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Binding.class));
  }
}
