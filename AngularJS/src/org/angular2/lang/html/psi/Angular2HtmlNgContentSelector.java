// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveSelector;
import org.jetbrains.annotations.NotNull;

public interface Angular2HtmlNgContentSelector extends PsiElement {

  @NotNull
  Angular2DirectiveSelector getSelector();
}
