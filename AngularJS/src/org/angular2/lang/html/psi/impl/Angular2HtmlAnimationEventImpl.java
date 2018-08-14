// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlAnimationEvent;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.intellij.openapi.util.Pair.pair;

public class Angular2HtmlAnimationEventImpl extends XmlAttributeImpl implements Angular2HtmlAnimationEvent {

  public Angular2HtmlAnimationEventImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitAnimationEvent(this);
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
  public String getAnimationEventName() {
    return getNameAndPhase().first;
  }

  @Nullable
  @Override
  public AnimationPhase getPhase() {
    return getNameAndPhase().second;
  }

  private Pair<String, AnimationPhase> getNameAndPhase() {
    String name = getName();
    if (name.startsWith("(") && name.endsWith(")")) {
      name = name.substring(1, name.length() - 1);
    }
    else if (name.startsWith("on-")) {
      name = name.substring(3);
    }
    else {
      throw new IllegalStateException("Bad attribute name: " + name);
    }
    if (name.startsWith("@")) {
      name = name.substring(1);
    }
    else {
      throw new IllegalStateException("Bad attribute name: " + name);
    }
    int dot = name.indexOf('.');
    if (dot < 0) {
      return pair(name, null);
    }
    String phase = name.substring(dot + 1).toLowerCase(Locale.ENGLISH);
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return pair(name, AnimationPhase.DONE);
    }
    else if ("start".equals(phase)) {
      return pair(name, AnimationPhase.START);
    }
    return pair(name, null);
  }

  @Nullable
  @Override
  public JSStatement getStatement() {
    return null;
  }
}
