package com.jetbrains.lang.dart;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.lang.dart.psi.DartBlock;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;

public class DartPsiTreeChangePreprocessor extends PsiTreeChangePreprocessorBase {
  public DartPsiTreeChangePreprocessor(@NotNull final PsiManager psiManager) {
    super((PsiManagerImpl)psiManager);
  }

  @Override
  public void treeChanged(@NotNull final PsiTreeChangeEventImpl event) {
    // Dart can be embedded in HTML
    if (event.getFile() instanceof DartFile || event.getFile() instanceof XmlFile) {
      super.treeChanged(event);
    }
  }

  @Override
  protected boolean isInsideCodeBlock(final PsiElement element) {
    if (element instanceof PsiFileSystemItem) return false;
    if (element == null || element.getParent() == null) return true;

    PsiElement parent = element;
    while (true) {
      if (parent == null || parent instanceof PsiFileSystemItem || parent instanceof DartEmbeddedContent) return false;
      if (parent instanceof DartBlock) return true;

      parent = parent.getParent();
    }
  }
}
