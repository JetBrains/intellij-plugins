package com.jetbrains.lang.dart.resolve;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartResolveProcessor extends DartPsiScopeProcessor {

  private final List<? super DartComponentName> myResult;
  private final String myName;
  private final boolean myLValue;

  public DartResolveProcessor(List<? super DartComponentName> result, @NotNull String name) {
    this(result, name, false);
  }

  public DartResolveProcessor(List<? super DartComponentName> result, @NotNull String name, boolean lookForLValue) {
    this.myResult = result;
    this.myName = name;
    this.myLValue = lookForLValue;
  }

  @Override
  protected boolean doExecute(final @NotNull DartComponentName componentName) {
    final PsiElement parentElement = componentName.getParent();

    if (parentElement instanceof DartComponent dartComponent) {
      // try set getter or get setter
      if (myLValue && dartComponent.isGetter()) return true;
      if (!myLValue && dartComponent.isSetter()) return true;
    }

    if (this.myName.equals(componentName.getName()) && !isMember(DartComponentType.typeOf(parentElement))) {
      myResult.add(componentName);
      return false;
    }

    return true;
  }

  private static boolean isMember(final @Nullable DartComponentType componentType) {
    return componentType == DartComponentType.CONSTRUCTOR ||
           componentType == DartComponentType.FIELD ||
           componentType == DartComponentType.METHOD;
  }
}
