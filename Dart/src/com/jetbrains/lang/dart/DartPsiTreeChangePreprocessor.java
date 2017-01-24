package com.jetbrains.lang.dart;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartPsiCompositeElement;
import com.jetbrains.lang.dart.psi.IDartBlock;
import org.jetbrains.annotations.NotNull;

public class DartPsiTreeChangePreprocessor extends PsiTreeChangePreprocessorBase {
  public DartPsiTreeChangePreprocessor(@NotNull PsiManager psiManager) {
    super(psiManager);
  }

  @Override
  protected boolean acceptsEvent(@NotNull PsiTreeChangeEventImpl event) {
    return event.getFile() instanceof DartFile || event.getFile() instanceof XmlFile;
  }

  @Override
  protected boolean isOutOfCodeBlock(@NotNull PsiElement element) {
    boolean result = false;
    for (PsiElement p : SyntaxTraverser.psiApi().parents(element)) {
      if (p instanceof IDartBlock) return false;
      if (p instanceof DartEmbeddedContent) break;
      if (p instanceof DartPsiCompositeElement) result = true;
    }
    return result;
  }
}
