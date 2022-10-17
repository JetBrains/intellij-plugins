// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.structure;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public  class DroolsFunctionStatementStructureViewElement extends DroolsStructureViewElement {
  public DroolsFunctionStatementStructureViewElement(DroolsFunctionStatement functionStatement) {
    super(functionStatement);
  }

  @Override
  public TreeElement @NotNull [] getChildren() {
    return EMPTY_ARRAY;
  }

  @Override
  public String getPresentableText() {
    final DroolsFunctionStatement statement = (DroolsFunctionStatement)getValue();
    if (!statement.isValid()) return null;

    final ItemPresentation presentation = ItemPresentationProviders.getItemPresentation(statement);
    return presentation == null ? statement.getFunctionName() : presentation.getPresentableText();
  }

  @Override
  public Icon getIcon(boolean unused) {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
  }
}
