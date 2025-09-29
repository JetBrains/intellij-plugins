// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;

@NotNullByDefault
public class MapParameterTypeManager implements ParameterTypeManager {
  public static final MapParameterTypeManager DEFAULT = new MapParameterTypeManager(STANDARD_PARAMETER_TYPES);

  private final Map<String, String> parameterTypesValues;
  private final @Nullable Map<String, SmartPsiElementPointer<PsiElement>> parameterTypesDeclarations;

  /// Creates a new [MapParameterTypeManager] that only holds parameter types that have a value
  /// (see doc of [ParameterTypeManager] for terminology).
  ///
  /// Sample value of `parameterTypesValues`:
  ///
  /// ```
  /// "" -> "(.*)"
  /// "float" -> "-?\d*[.,]?\d+"
  /// "word" -> "[^\s]+"
  /// "int" -> "-?\d+"
  /// ```
  public MapParameterTypeManager(Map<String, String> parameterTypesValues) {
    this(parameterTypesValues, null);
  }

  /// Creates a new [MapParameterTypeManager] that holds parameter types that have a value and parameter type that have a declaration.
  /// (see doc of [ParameterTypeManager] for terminology).
  ///
  /// Sample value of `parameterTypeDeclarations`:
  /// ```
  /// isoDate -> SmartPointer to PsiIdentifier:isoDate (getNameIdentifier() of PsiMethod:isoDate)
  /// ```
  public MapParameterTypeManager(Map<String, String> parameterTypesValues,
                                 @Nullable Map<String, SmartPsiElementPointer<PsiElement>> parameterTypeDeclarations) {
    this.parameterTypesValues = parameterTypesValues;
    parameterTypesDeclarations = parameterTypeDeclarations;
  }

  @Override
  public @Nullable String getParameterTypeValue(String name) {
    return parameterTypesValues.get(name);
  }

  @Override
  public @Nullable PsiElement getParameterTypeDeclaration(String name) {
    if (parameterTypesDeclarations == null) {
      return null;
    }
    SmartPsiElementPointer<PsiElement> smartPointer = parameterTypesDeclarations.get(name);
    return smartPointer != null ? smartPointer.getElement() : null;
  }
}
