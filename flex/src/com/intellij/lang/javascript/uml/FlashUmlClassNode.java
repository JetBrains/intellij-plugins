// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.PsiDiagramNode;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

public class FlashUmlClassNode extends PsiDiagramNode {
  public FlashUmlClassNode(final JSClass clazz, DiagramProvider provider) {
    super(clazz, provider);
  }

  @Override
  public String getTooltip() {
    return "<html><b>" + ((JSClass)getElement()).getQualifiedName() + "</b></html>";
  }
}
