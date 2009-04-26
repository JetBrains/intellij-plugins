/*
 * Copyright 2008 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.dom.struts.impl.path;

import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import com.intellij.codeInsight.lookup.LookupValueWithPsiElement;
import com.intellij.codeInsight.lookup.LookupValueWithUIHint;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a lookup value for an Action.
 *
 * @author Yann C&eacute;bron
 */
class ActionLookupItem implements LookupValueWithUIHint,
                                  LookupValueWithPriority,
                                  LookupValueWithPsiElement,
                                  Iconable {

  private final Action action;
  private final boolean bold;

  /**
   * CTOR.
   *
   * @param action Action to build the lookup element for.
   * @param bold   Whether to render this element in bold.
   */
  ActionLookupItem(final Action action, final boolean bold) {
    this.action = action;
    this.bold = bold;
  }

  public String getTypeHint() {
    return action.getNamespace();
  }

  public Color getColorHint() {
    return null;
  }

  public boolean isBold() {
    return bold;
  }

  public String getPresentation() {
    return action.getName().getStringValue();
  }

  /**
   * Sort only actions of current package first, rest behind everything else.
   *
   * @return 1 or -1.
   */
  public int getPriority() {
    return bold ? HIGHER : -1;
  }

  public Icon getIcon(final int flags) {
    return StrutsIcons.ACTION;
  }

  public PsiElement getElement() {
    return action.getXmlTag();
  }

}