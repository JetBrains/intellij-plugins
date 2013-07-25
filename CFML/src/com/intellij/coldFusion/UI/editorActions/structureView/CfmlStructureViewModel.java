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
