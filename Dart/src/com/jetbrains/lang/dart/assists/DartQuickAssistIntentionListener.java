// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public interface DartQuickAssistIntentionListener {
  void onQuickAssistIntentionInvoked(@NotNull DartQuickAssistIntention intention, @NotNull Editor editor, @NotNull PsiFile file);
}
