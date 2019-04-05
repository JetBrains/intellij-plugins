// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public interface DartQuickFixListener {
  public void onQuickFixInvoked(
      DartQuickFix intention,
      Editor editor,
      PsiFile file);
}
