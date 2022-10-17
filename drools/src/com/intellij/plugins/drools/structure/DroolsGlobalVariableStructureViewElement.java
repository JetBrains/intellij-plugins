package com.intellij.plugins.drools.structure;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.plugins.drools.lang.psi.DroolsGlobalStatement;
import com.intellij.psi.PsiType;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public  class DroolsGlobalVariableStructureViewElement extends DroolsStructureViewElement {
  public DroolsGlobalVariableStructureViewElement(DroolsGlobalStatement globalStatement) {
    super(globalStatement);
  }

  @Override
  public TreeElement @NotNull [] getChildren() {
    return EMPTY_ARRAY;
  }

  @Override
  public String getPresentableText() {
    final DroolsGlobalStatement globalStatement = (DroolsGlobalStatement)getValue();
    if (!globalStatement.isValid()) return null;

    final String name = globalStatement.getName();
    final PsiType type = globalStatement.getType();

    return name + " : " + type.getPresentableText();
  }

  @Override
  public Icon getIcon(boolean unused) {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
  }
}
