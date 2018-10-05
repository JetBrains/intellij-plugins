package com.jetbrains.lang.dart.resolve;

import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClassNameScopeProcessor extends DartPsiScopeProcessor {

  private final @NotNull Set<? super DartComponentName> myResult;

  public ClassNameScopeProcessor(final @NotNull Set<? super DartComponentName> result) {
    this.myResult = result;
  }

  @Override
  protected boolean doExecute(final @NotNull DartComponentName componentName) {
    if (componentName.getParent() instanceof DartClass) myResult.add(componentName);
    return true;
  }
}
