// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;

public class MapParameterTypeManager implements ParameterTypeManager {
  public static final MapParameterTypeManager DEFAULT = new MapParameterTypeManager(STANDARD_PARAMETER_TYPES);

  private final Map<String, String> myParameterTypes;
  private final Map<String, SmartPsiElementPointer<PsiElement>> myParameterTypeDeclarations;

  public MapParameterTypeManager(Map<String, String> parameterTypes) {
    this(parameterTypes, null);
  }

  public MapParameterTypeManager(Map<String, String> parameterTypes, Map<String, SmartPsiElementPointer<PsiElement>> parameterTypeDeclarations) {
    myParameterTypes = parameterTypes;
    myParameterTypeDeclarations = parameterTypeDeclarations;
  }

  @Override
  public @Nullable String getParameterTypeValue(@NotNull String name) {
    return myParameterTypes.get(name);
  }

  @Override
  public PsiElement getParameterTypeDeclaration(@NotNull String name) {
    if (myParameterTypeDeclarations == null) {
      return null;
    }
    SmartPsiElementPointer<PsiElement> smartPointer = myParameterTypeDeclarations.get(name);
    return smartPointer != null ? smartPointer.getElement() : null;
  }
}
