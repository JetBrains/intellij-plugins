package com.dmarcotte.handlebars.structure;

import com.dmarcotte.handlebars.psi.HbBlockWrapper;
import com.dmarcotte.handlebars.psi.HbPlainMustache;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class HbStructureViewModel extends TextEditorBasedStructureViewModel {
  private final HbPsiFile myFile;
  // classes which we construct structure view nodes for
  static final Class[] ourSuitableClasses = new Class[]{HbBlockWrapper.class, HbPlainMustache.class};

  public HbStructureViewModel(@NotNull HbPsiFile psiFile, @Nullable Editor editor) {
    super(editor, psiFile);
    this.myFile = psiFile;
  }

  @NotNull
  @Override
  protected Class[] getSuitableClasses() {
    return ourSuitableClasses;
  }

  @NotNull
  @Override
  public StructureViewTreeElement getRoot() {
    return new HbTreeElementFile(myFile);
  }
}
