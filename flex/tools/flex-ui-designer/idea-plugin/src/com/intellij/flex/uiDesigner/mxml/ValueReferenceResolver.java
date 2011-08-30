package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.lang.javascript.psi.JSVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface ValueReferenceResolver {
  @Nullable
  VariableReference getValueReference(JSVariable jsVariable);
  @NotNull
  MxmlObjectReference getValueReference(String id) throws InvalidPropertyException;
}
