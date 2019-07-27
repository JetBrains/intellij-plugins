// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.resolve.accessibility.TypeScriptConfigAccessibilityChecker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.Nullable;

public class Angular2ConfigAccessibilityChecker extends TypeScriptConfigAccessibilityChecker {

  @Override
  protected VirtualFile getScope(@Nullable PsiElement place) {
    return place == null ? null : super.getScope(Angular2IndexingHandler.findComponentClass(place));
  }
}
