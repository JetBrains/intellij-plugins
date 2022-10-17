// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.structure;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.plugins.drools.lang.psi.DroolsRuleStatement;
import com.intellij.plugins.drools.JbossDroolsIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public  class DroolsRuleStatementStructureViewElement extends DroolsStructureViewElement {
  public DroolsRuleStatementStructureViewElement(DroolsRuleStatement ruleStatement) {
    super(ruleStatement);
  }

  @Override
  public TreeElement @NotNull [] getChildren() {
    return EMPTY_ARRAY;
  }

  @Override
  public String getPresentableText() {
    return ((DroolsRuleStatement)getValue()).getRuleName().getText();
  }

  @Override
  public Icon getIcon(boolean unused) {
    return JbossDroolsIcons.Drools_16;
  }
}
