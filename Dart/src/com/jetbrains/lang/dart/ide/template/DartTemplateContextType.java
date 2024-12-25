// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighter;
import com.jetbrains.lang.dart.psi.IDartBlock;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartTemplateContextType extends TemplateContextType {
  protected DartTemplateContextType(@NotNull @NlsContexts.Label String presentableName) {
    super(presentableName);
  }

  /**
   * @deprecated Set contextId and baseContextId in plugin.xml instead
   */
  @Deprecated
  protected DartTemplateContextType(@NotNull @NonNls String id,
                                    @NotNull @NlsContexts.Label String presentableName,
                                    @Nullable Class<? extends TemplateContextType> baseContextType) {
    super(id, presentableName, baseContextType);
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    if (file.getLanguage() instanceof DartLanguage) {
      PsiElement element = file.findElementAt(offset);
      return element != null && isInContext(element);
    }
    return false;
  }

  protected abstract boolean isInContext(@NotNull PsiElement element);

  @Override
  public @Nullable SyntaxHighlighter createHighlighter() {
    return new DartSyntaxHighlighter();
  }

  public static final class Generic extends DartTemplateContextType {
    public Generic() {
      super(DartBundle.message("template.context.type.dart"));
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return true;
    }
  }

  public static final class Statement extends DartTemplateContextType {
    public Statement() {
      super(DartBundle.message("template.context.type.dart.statement"));
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return PsiTreeUtil.getNonStrictParentOfType(element, IDartBlock.class, PsiComment.class) instanceof IDartBlock;
    }
  }
}
