// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlStructureViewModel extends TextEditorBasedStructureViewModel {
  private final StructureViewTreeElement myRoot;
  private final Class[] myClasses = {CfmlFunction.class};

  protected CfmlStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    super(editor, psiFile);
    myRoot = new CfmlStructureViewElement(getPsiFile());
  }

  @Override
  public @NotNull StructureViewTreeElement getRoot() {
    return myRoot;
  }

  @Override
  public Sorter @NotNull [] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }

  @Override
  protected Class @NotNull [] getSuitableClasses() {
    return myClasses;
  }
}
