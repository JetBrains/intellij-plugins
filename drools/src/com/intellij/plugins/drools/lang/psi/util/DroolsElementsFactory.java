// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.openapi.project.Project;
import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DroolsElementsFactory {

  @Nullable
  public static DroolsNameId createPatternBindIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project)
      .createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "rule foo when " + name + ": Foo() then aaa");

    final PsiElement deepestElement = psiFile.findElementAt(15);
    if (deepestElement != null && deepestElement.getParent() instanceof DroolsNameId) {
      return (DroolsNameId)deepestElement.getParent();
    }

    return null;
  }

  @Nullable
  public static DroolsTypeName createDeclaredTypeNameIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project)
      .createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "declare " + name + " end");

    final DroolsDeclareStatement[] declarations = psiFile.getDeclarations();
    if (declarations.length == 1) {
      DroolsTypeDeclaration typeDeclaration = declarations[0].getTypeDeclaration();
      if (typeDeclaration != null) return typeDeclaration.getTypeName();
    }

    return null;
  }

  @Nullable
  public static DroolsFieldName createFieldNameIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project)
      .createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "declare Foo " + name + " : int end");

    final DroolsDeclareStatement[] declarations = psiFile.getDeclarations();
    if (declarations.length == 1) {
      DroolsTypeDeclaration typeDeclaration = declarations[0].getTypeDeclaration();
      if (typeDeclaration != null) {
        List<DroolsField> fields = typeDeclaration.getFieldList();
        if (fields.size() == 1) {
          return fields.get(0).getFieldName();
        }
      }
    }

    return null;
  }

  @Nullable
  public static DroolsNameId createFunctionNameIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project)
      .createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "function void " + name + "(){}");

    final PsiElement deepestElement = psiFile.findElementAt(15);
    if (deepestElement != null && deepestElement.getParent() instanceof DroolsNameId) {
      return (DroolsNameId)deepestElement.getParent();
    }

    return null;
  }

  @Nullable
  public static DroolsStringId createQueryNameIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project)
      .createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "query " + name + " end");

    return psiFile.getQueries()[0].getStringId();
  }

  @Nullable
  public static DroolsIdentifier createDroolsIdentifier(@NotNull String name, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project).
      createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "import " + name);
    final PsiElement deepestElement = psiFile.findElementAt(8);
    if (deepestElement != null) {
      final PsiElement element = deepestElement.getParent();
      if (element instanceof DroolsIdentifier) {
        return (DroolsIdentifier)element;
      }
    }
    return null;
  }

  @Nullable
  public static DroolsImport createDroolsImport(@NotNull String className, @NotNull Project project) {
    final DroolsFile psiFile = (DroolsFile)PsiFileFactory.getInstance(project).
      createFileFromText("_dummy.drl", DroolsFileType.DROOLS_FILE_TYPE, "import " + className+";");
    final DroolsImport[] imports = psiFile.getImports();
    if (imports.length == 1) {
        return imports[0];
    }
    return null;
  }
}
