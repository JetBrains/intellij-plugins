package com.dmarcotte.handlebars.structure;

import com.dmarcotte.handlebars.psi.HbBlockWrapper;
import com.dmarcotte.handlebars.psi.HbPlainMustache;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class HbStructureViewModel extends TextEditorBasedStructureViewModel {
  static final Class[] ourSuitableClasses = new Class[]{HbBlockWrapper.class, HbPlainMustache.class};

  HbStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    super(editor, psiFile);
  }

  @Override
  protected Class @NotNull [] getSuitableClasses() {
    return ourSuitableClasses;
  }

  @NotNull
  @Override
  public StructureViewTreeElement getRoot() {
    return new HbTreeElementFile((HbPsiFile)getPsiFile());
  }
}
