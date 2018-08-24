// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Action;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.lang.html.parser.Angular2HtmlParsing.normalizeAttributeName;

public class Angular2HtmlEventImpl extends Angular2HtmlBoundAttributeImpl implements Angular2HtmlEvent {

  public Angular2HtmlEventImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitEvent(this);
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
  public String getEventName() {
    return getNameAndPhase().first;
  }

  @NotNull
  @Override
  public EventType getEventType() {
    return getNameAndPhase().second == null
           ? EventType.REGULAR : EventType.ANIMATION;
  }

  @Nullable
  @Override
  public AnimationPhase getAnimationPhase() {
    return getNameAndPhase().second;
  }

  private Pair<String, AnimationPhase> getNameAndPhase() {
    String name = normalizeAttributeName(getName());
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
    } else if (name.startsWith("animate-")) {
      name = name.substring(8);
    } else {
      return pair(name, null);
    }
    int dot = name.indexOf('.');
    if (dot < 0) {
      return pair(name, AnimationPhase.INVALID);
    }
    String phase = name.substring(dot + 1).toLowerCase(Locale.ENGLISH);
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return pair(name, AnimationPhase.DONE);
    }
    else if ("start".equals(phase)) {
      return pair(name, AnimationPhase.START);
    }
    return pair(name, AnimationPhase.INVALID);
  }

  @Nullable
  @Override
  public Angular2Action getAction() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Action.class));
  }

  @Override
  public String toString() {
    return "Angular2HtmlEvent <" + getEventName() + ", " + getEventType()
           + (getEventType() == EventType.ANIMATION ?  ", "  + getAnimationPhase() : "") + ">";
  }

}
