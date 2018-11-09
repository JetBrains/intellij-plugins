// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.html.psi.Angular2HtmlFormCaseContent;

public class Angular2HtmlExpansionFormCaseContentImpl extends CompositePsiElement implements Angular2HtmlFormCaseContent {

  public Angular2HtmlExpansionFormCaseContentImpl(IElementType type) {
    super(type);
  }
}
