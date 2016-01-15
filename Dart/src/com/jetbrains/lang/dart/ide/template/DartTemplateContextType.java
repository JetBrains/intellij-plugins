package com.jetbrains.lang.dart.ide.template;

import com.intellij.codeInsight.template.EverywhereContextType;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighter;
import com.jetbrains.lang.dart.psi.DartBlock;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartTemplateContextType extends TemplateContextType {
  protected DartTemplateContextType(@NotNull @NonNls String id,
                                    @NotNull String presentableName,
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

  @Nullable
  @Override
  public SyntaxHighlighter createHighlighter() {
    return new DartSyntaxHighlighter();
  }

  public static class Generic extends DartTemplateContextType {
    public Generic() {
      super("DART", DartBundle.message("dart.language.id"), EverywhereContextType.class);
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return true;
    }
  }

  public static class Statement extends DartTemplateContextType {
    public Statement() {
      super("DART_STATEMENT", "Statement", Generic.class);
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return PsiTreeUtil.getParentOfType(element, DartBlock.class) != null;
    }
  }
}
