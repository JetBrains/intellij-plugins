// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.DartExpressionCodeFragmentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartElementGenerator {
  public static @Nullable DartReference createReferenceFromText(Project myProject, String text) {
    final DartExpression expression = createExpressionFromText(myProject, text);
    return expression instanceof DartReference ? (DartReference)expression : null;
  }

  public static @Nullable DartExpression createExpressionFromText(Project myProject, String text) {
    final PsiFile file = createDummyFile(myProject, "var dummy = " + text + ";");
    final PsiElement child = file.getFirstChild();
    if (child instanceof DartVarDeclarationList) {
      final DartVarInit varInit = ((DartVarDeclarationList)child).getVarInit();
      return varInit == null ? null : varInit.getExpression();
    }
    return null;
  }

  public static PsiFile createExpressionCodeFragment(Project myProject, String text, PsiElement context) {
    final String name = "dummy." + DartFileType.DEFAULT_EXTENSION;
    final DartExpressionCodeFragmentImpl codeFragment = new DartExpressionCodeFragmentImpl(myProject, name, text, true);
    codeFragment.setContext(context);
    return codeFragment;
  }

  public static @Nullable PsiElement createStatementFromText(Project myProject, String text) {
    final PsiFile file = createDummyFile(myProject, "dummy(){" + text + "}");
    final PsiElement child = file.getFirstChild();
    if (child instanceof DartFunctionDeclarationWithBodyOrNative) {
      final DartFunctionBody functionBody = ((DartFunctionDeclarationWithBodyOrNative)child).getFunctionBody();
      final IDartBlock block = PsiTreeUtil.getChildOfType(functionBody, IDartBlock.class);
      final DartStatements statements = block == null ? null : block.getStatements();
      return statements == null ? null : statements.getFirstChild();
    }
    return null;
  }


  public static @Nullable DartId createIdentifierFromText(Project myProject, String name) {
    final PsiFile dummyFile = createDummyFile(myProject, name + "(){}");
    final DartComponent dartComponent = PsiTreeUtil.getChildOfType(dummyFile, DartComponent.class);
    final DartComponentName componentName = dartComponent == null ? null : dartComponent.getComponentName();
    return componentName == null ? null : componentName.getId();
  }

  public static @Nullable DartLibraryNameElement createLibraryNameElementFromText(final @NotNull Project project, final @NotNull String libraryName) {
    final PsiFile dummyFile = createDummyFile(project, "library " + libraryName + ";");
    final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(dummyFile, DartLibraryStatement.class);
    return libraryStatement == null ? null : libraryStatement.getLibraryNameElement();
  }


  public static PsiFile createDummyFile(Project myProject, String text) {
    final PsiFileFactory factory = PsiFileFactory.getInstance(myProject);
    final String name = "dummy." + DartFileType.INSTANCE.getDefaultExtension();
    final LightVirtualFile virtualFile = new LightVirtualFile(name, DartFileType.INSTANCE, text);
    final PsiFile psiFile = ((PsiFileFactoryImpl)factory).trySetupPsiForFile(virtualFile, DartLanguage.INSTANCE, false, true);
    assert psiFile != null;
    return psiFile;
  }
}
