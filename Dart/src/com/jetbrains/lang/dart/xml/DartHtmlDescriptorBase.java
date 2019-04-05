// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiMetaData;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartHtmlDescriptorBase implements PsiMetaData {
  @NotNull private final Project myProject;
  @NotNull private final String myName;
  @NotNull private final DartServerData.DartNavigationTarget myTarget;

  public DartHtmlDescriptorBase(@NotNull Project project, @NotNull String name, @NotNull DartServerData.DartNavigationTarget target) {
    myProject = project;
    myName = name;
    myTarget = target;
  }

  @Nullable
  @Override
  public final PsiElement getDeclaration() {
    return DartResolver.getElementForNavigationTarget(myProject, myTarget);
  }

  @NotNull
  @Override
  public final String getName() {
    return myName;
  }

  @Override
  public final String getName(PsiElement context) {
    return getName();
  }

  @Override
  public final void init(PsiElement element) {}
}
