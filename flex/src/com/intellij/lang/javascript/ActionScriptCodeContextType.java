package com.intellij.lang.javascript;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCodeContextType extends TemplateContextType {

  @NonNls private static final String ACTION_SCRIPT = "ACTION_SCRIPT";

  public ActionScriptCodeContextType() {
    super(ACTION_SCRIPT, JSBundle.message("actionscript.template.context.type"));
  }

  @Override
  public boolean isInContext(final @NotNull PsiFile file, int offset) {
    if (file.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
      return true;
    }

    final Language language = JavaScriptCodeContextType.getLanguage(file, offset);
    return language != null && language.isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  @Override
  public boolean isInContext(final @NotNull FileType fileType) {
    return fileType instanceof JavaScriptFileType;
  }
}
