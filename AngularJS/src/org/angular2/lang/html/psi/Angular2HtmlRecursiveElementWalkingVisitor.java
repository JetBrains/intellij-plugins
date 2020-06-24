// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWalkingState;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlRecursiveElementWalkingVisitor extends Angular2HtmlElementVisitor {

  private final PsiWalkingState myWalkingState = new PsiWalkingState(this) {
  };

  @Override
  public void visitElement(final @NotNull PsiElement element) {
    myWalkingState.elementStarted(element);
  }

  public void stopWalking() {
    myWalkingState.stopWalking();
  }
}
