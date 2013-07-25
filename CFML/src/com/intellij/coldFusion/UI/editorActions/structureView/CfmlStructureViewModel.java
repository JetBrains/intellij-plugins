/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 19.02.2009
 */
public class CfmlStructureViewModel extends TextEditorBasedStructureViewModel {
  private PsiFile myCfmlPsiFile;
  private StructureViewTreeElement myRoot;
  private final Class[] myClasses = {CfmlFunction.class};

  protected CfmlStructureViewModel(@NotNull PsiFile psiFile) {
    super(psiFile);

    myCfmlPsiFile = psiFile.getViewProvider().getPsi(CfmlLanguage.INSTANCE);
    myRoot = new CfmlStructureViewElement(myCfmlPsiFile);
  }

  @Override
  protected PsiFile getPsiFile() {
    return myCfmlPsiFile;
  }

  @Override
  @NotNull
  public StructureViewTreeElement getRoot() {
    return myRoot;
  }

  @Override
  @NotNull
  public Grouper[] getGroupers() {
    return new Grouper[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }

  @Override
  @NotNull
  public Filter[] getFilters() {
    return new Filter[0];
  }

  @NotNull
  @Override
  protected Class[] getSuitableClasses() {
    return myClasses;
  }
}
