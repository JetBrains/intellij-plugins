package com.jetbrains.lang.dart.resolve;

import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ComponentNameScopeProcessor extends DartPsiScopeProcessor {

  private final @NotNull Set<? super DartComponentName> myResult;

  public ComponentNameScopeProcessor(final @NotNull Set<? super DartComponentName> result) {
    this.myResult = result;
  }

  @Override
  protected boolean doExecute(final @NotNull DartComponentName componentName) {
    myResult.add(componentName);
    return true;
  }
}
