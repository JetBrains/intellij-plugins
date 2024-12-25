// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.plugins.drools.DroolsBundle;
import com.intellij.plugins.drools.JbossDroolsIcons;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DroolsStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public DroolsStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    super(psiFile, editor, new DroolsFileStructureViewElement((DroolsFile)psiFile));
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public Filter @NotNull [] getFilters() {
    return new Filter[]{myRuleFilter, myGlobalVarFilter, myFunctionFilter};
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return false;
  }

  private static final Filter myGlobalVarFilter = new Filter() {
    public static final @NonNls String ID = "SHOW_VARIABLES";

    @Override
    public boolean isVisible(TreeElement treeNode) {
      return !(treeNode instanceof DroolsGlobalVariableStructureViewElement);
    }

    @Override
    public boolean isReverted() {
      return true;
    }

    @Override
    public @NotNull ActionPresentation getPresentation() {
      return new ActionPresentationData(
        DroolsBundle.message("action.structure.view.show.variables"),
        null,
        IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable)
      );
    }

    @Override
    public @NotNull String getName() {
      return ID;
    }
  };

  private static final Filter myFunctionFilter = new Filter() {
    public static final @NonNls String ID = "SHOW_FUNCTIONS";

    @Override
    public boolean isVisible(TreeElement treeNode) {
      return !(treeNode instanceof DroolsFunctionStatementStructureViewElement);
    }

    @Override
    public boolean isReverted() {
      return true;
    }

    @Override
    public @NotNull ActionPresentation getPresentation() {
      return new ActionPresentationData(
        DroolsBundle.message("action.structure.view.show.functions"),
        null,
        IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method)
      );
    }

    @Override
    public @NotNull String getName() {
      return ID;
    }
  };

  private static final Filter myRuleFilter = new Filter() {
    public static final @NonNls String ID = "SHOW_RULES";

    @Override
    public boolean isVisible(TreeElement treeNode) {
      return !(treeNode instanceof DroolsRuleStatementStructureViewElement);
    }

    @Override
    public boolean isReverted() {
      return true;
    }

    @Override
    public @NotNull ActionPresentation getPresentation() {
      return new ActionPresentationData(
        DroolsBundle.message("action.structure.view.show.rules"),
        null,
        JbossDroolsIcons.Drools_16
      );
    }

    @Override
    public @NotNull String getName() {
      return ID;
    }
  };
}
