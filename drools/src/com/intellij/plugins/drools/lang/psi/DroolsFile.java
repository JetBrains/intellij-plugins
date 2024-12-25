// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.FileASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypeSets;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiImportHolder;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DroolsFile extends PsiFileBase implements PsiImportHolder {
  public DroolsFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, DroolsLanguage.INSTANCE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return DroolsFileType.DROOLS_FILE_TYPE;
  }

  @Override
  public String toString() {
    return "Drools File";
  }

  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }

  public DroolsDeclareStatement @NotNull [] getDeclarations() {
    return findChildrenByClass(DroolsDeclareStatement.class);
  }

  public DroolsRuleStatement @NotNull [] getRules() {
    return findChildrenByClass(DroolsRuleStatement.class);
  }

  public DroolsFunctionStatement @NotNull [] getFunctions() {
    return findChildrenByClass(DroolsFunctionStatement.class);
  }

  public DroolsQueryStatement @NotNull [] getQueries() {
    return findChildrenByClass(DroolsQueryStatement.class);
  }

  public DroolsImportStatement[] getImports() {
    return findChildrenByClass(DroolsImportStatement.class);
  }

  public DroolsGlobalStatement[] getGlobalVariables() {
    return findChildrenByClass(DroolsGlobalStatement.class);
  }

  public @Nullable DroolsPackageStatement getPackage() {
    return findChildByClass(DroolsPackageStatement.class);
  }

  public DroolsAttribute @NotNull [] getAttributes() {
    return findChildrenByClass(DroolsAttribute.class);
  }

  public @Nullable DroolsUnitStatement getUnitStatement() {
    return findChildByClass(DroolsUnitStatement.class);
  }

  public @Nullable DroolsAttribute findAttributeByName(@NotNull String name) {
    for (DroolsAttribute attribute : getAttributes()) {
      if (name.equals(attribute.getAttributeName())) return attribute;
    }
    return null;
  }

  @Override
  public boolean importClass(@NotNull PsiClass aClass) {
    final String qName = aClass.getQualifiedName();
    if (qName == null) return false;

    for (DroolsImport anImport : getImports()) {
      if (qName.equals(anImport.getImportedClassName())) return true;
    }
    try {
      DroolsImport importStatement = DroolsElementsFactory.createDroolsImport(qName, getProject());
      if (importStatement != null) {
        return addImport(importStatement) != null;
      }
    }
    catch (IncorrectOperationException e) {
      return false;
    }
    return false;
  }

  public DroolsImport addImport(@NotNull DroolsImport droolsImport) throws IncorrectOperationException {
    PsiElement anchor = getAnchorToInsertImportAfter(droolsImport);
    final FileASTNode node = getNode();
    PsiElement result = null;
    if (anchor == null) {
      PsiElement psiElement = getFirstChild();
      if (psiElement != null) {
        result = addBefore(droolsImport, psiElement);
        PsiElement sibling = result.getNextSibling();
        if (sibling != null) {
          node.addLeaf(DroolsTokenTypeSets.NEW_LINE, StringUtil.repeat("\n", 1), sibling.getNode());
        }
      }
    }
    else {
      result = addAfter(droolsImport, anchor);
      node.addLeaf(DroolsTokenTypeSets.NEW_LINE, StringUtil.repeat("\n", 1), result.getNode());
    }

    return (DroolsImport)result;
  }

  private @Nullable PsiElement getAnchorToInsertImportAfter(@NotNull DroolsImport droolsImport) {
    DroolsImport[] importStatements = getImports();
    if (importStatements.length == 0) {
      final DroolsPackageStatement aPackage = getPackage();
      if (aPackage != null) {
        return aPackage;
      }
    }
    else {
      return importStatements[importStatements.length - 1];
    }
    return null;
  }
}
