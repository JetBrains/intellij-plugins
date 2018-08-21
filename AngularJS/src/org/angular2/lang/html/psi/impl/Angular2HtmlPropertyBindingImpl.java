// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2Interpolation;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.lang.html.parser.Angular2HtmlParsing.normalizeAttributeName;

public class Angular2HtmlPropertyBindingImpl extends Angular2HtmlBaseAttributeImpl implements Angular2HtmlPropertyBinding {

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
    return getNameAndType().first;
  }

  @NotNull
  @Override
  public PropertyBindingType getBindingType() {
    return getNameAndType().second;
  }

  private Pair<String, PropertyBindingType> getNameAndType() {
    String name = normalizeAttributeName(getName());
    if (name.startsWith("[") && name.endsWith("]")) {
      name = name.substring(1, name.length() - 1);
      if (name.startsWith("@")) {
        return pair(name.substring(1), PropertyBindingType.ANIMATION);
      }
    }
    else if (name.startsWith("bind-")) {
      name = name.substring(5);
      if (name.startsWith("animate-")) {
        return pair(name.substring(8), PropertyBindingType.ANIMATION);
      }
    }
    else if (name.startsWith("@")) {
      return pair(name.substring(1), PropertyBindingType.ANIMATION);
    }
    if (name.startsWith("attr.")) {
      return pair(name.substring(5), PropertyBindingType.ATTRIBUTE);
    }
    if (name.startsWith("class.")) {
      return pair(name.substring(6), PropertyBindingType.CLASS);
    }
    if (name.startsWith("style.")) {
      return pair(name.substring(6), PropertyBindingType.STYLE);
    }
    return pair(name, PropertyBindingType.PROPERTY);
  }

  @Nullable
  @Override
  public Angular2Binding getBinding() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Binding.class));
  }

  @NotNull
  @Override
  public Angular2Interpolation[] getInterpolations() {
    return PsiTreeUtil.findChildrenOfType(this, Angular2Interpolation.class)
      .toArray(new Angular2Interpolation[0]);
  }

  @Override
  public String toString() {
    return "Angular2HtmlPropertyBinding " + getNameAndType();
  }

}
