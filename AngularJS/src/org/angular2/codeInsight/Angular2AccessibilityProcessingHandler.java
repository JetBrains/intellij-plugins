// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler;
import com.intellij.lang.javascript.psi.resolve.accessibility.JSAccessibilityChecker;
import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Angular2AccessibilityProcessingHandler extends AccessibilityProcessingHandler {

  private static final List<JSAccessibilityChecker> ourCheckers;

  static {
    List<JSAccessibilityChecker> checkers = ContainerUtil.filter(
      CHECKERS, checker -> !(checker instanceof TypeScriptConfigAccessibilityChecker));
    checkers.add(new Angular2ConfigAccessibilityChecker());
    ourCheckers = ContainerUtil.immutableList(checkers);
  }

  public Angular2AccessibilityProcessingHandler(@Nullable PsiElement _place) {
    super(_place);
  }

  @Override
  protected Collection<JSAccessibilityChecker> getCheckers() {
    return ourCheckers;
  }
}
