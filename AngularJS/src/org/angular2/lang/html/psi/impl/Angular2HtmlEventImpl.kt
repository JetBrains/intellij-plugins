// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Action;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.EventInfo;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Override
  public @NotNull String getEventName() {
    return getAttributeInfo().name;
  }

  @Override
  public @NotNull EventType getEventType() {
    return ((EventInfo)getAttributeInfo()).eventType;
  }

  @Override
  public @Nullable AnimationPhase getAnimationPhase() {
    return ((EventInfo)getAttributeInfo()).animationPhase;
  }

  @Override
  public @Nullable Angular2Action getAction() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Action.class));
  }
}
