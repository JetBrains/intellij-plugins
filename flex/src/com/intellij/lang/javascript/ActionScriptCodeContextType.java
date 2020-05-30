// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.liveTemplates.JSLikeTemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCodeContextType extends TemplateContextType implements JSLikeTemplateContextType {

  @NonNls private static final String ACTION_SCRIPT = "ACTION_SCRIPT";

  public ActionScriptCodeContextType() {
    super(ACTION_SCRIPT, JavaScriptBundle.message("actionscript.template.context.type"));
  }

  @Override
  public boolean isInContext(final @NotNull PsiFile file, int offset) {
    if (file.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
      return true;
    }

    final Language language = JSLanguageUtil.getLanguage(file.findElementAt(offset));
    return language != null && language.isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }
}
