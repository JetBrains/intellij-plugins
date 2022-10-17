// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.structure;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.plugins.drools.lang.psi.DroolsGlobalStatement;
import com.intellij.plugins.drools.lang.psi.DroolsRuleStatement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DroolsFileStructureViewElement extends DroolsStructureViewElement {
  private final DroolsFile myPsiFile;

  public DroolsFileStructureViewElement(DroolsFile psiFile) {
    super(psiFile);
    myPsiFile = psiFile;
  }

  @Override
  public TreeElement @NotNull [] getChildren() {
    final List<TreeElement> result = new ArrayList<>();

    for (DroolsGlobalStatement globalStatement : myPsiFile.getGlobalVariables()) {
      result.add(new DroolsGlobalVariableStructureViewElement(globalStatement));
    }

    for (DroolsRuleStatement ruleStatement : myPsiFile.getRules()) {
      result.add(new DroolsRuleStatementStructureViewElement(ruleStatement));
    }

   for (DroolsFunctionStatement functionStatement : myPsiFile.getFunctions()) {
      result.add(new DroolsFunctionStatementStructureViewElement(functionStatement));
    }

    return result.toArray(TreeElement.EMPTY_ARRAY);
  }

  @Override
  public String getPresentableText() {
    final ItemPresentation presentation = myPsiFile.getPresentation();
    return presentation == null ? null : presentation.getPresentableText();
  }

  @Override
  public String getLocationString() {
    final ItemPresentation presentation = myPsiFile.getPresentation();
    return presentation == null ? null : presentation.getLocationString();
  }

  @Override
  public Icon getIcon(boolean unused) {
    final ItemPresentation presentation = myPsiFile.getPresentation();
    return presentation == null ? null : presentation.getIcon(unused);
  }
}
