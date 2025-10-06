// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Reference to itself for the name element of Cucumber Parameter Type declaration
///
/// ```ruby
/// ParameterType(
///   name: 'anything',
///   regexp: /(.*?)/,
/// )
/// ```
///
/// It's necessary for "Find Usages" to work for custom parameter types.
///
/// Currently only used in Cucumber Ruby.
/// Probably could also be used in Cucumber Java (and others).
///
/// @see org.jetbrains.plugins.cucumber.ParameterTypeManager ParameterTypeManager
/// @see org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterTypeReference CucumberJavaParameterTypeReference
@NotNullByDefault
public class CucumberParameterTypeSelfReference extends PsiReferenceBase<PsiElement> {
  public CucumberParameterTypeSelfReference(PsiElement element) {
    super(element, TextRange.create(1, element.getTextLength() - 1));
  }

  @Override
  public @Nullable PsiElement resolve() {
    return getElement();
  }
}
