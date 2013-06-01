package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class ResolveScopeProcessor implements PsiScopeProcessor {
  private final List<DartComponentName> result;
  private final String name;
  private final boolean isLValue;

  public ResolveScopeProcessor(List<DartComponentName> result, @NotNull String name) {
    this(result, name, false);
  }

  public ResolveScopeProcessor(List<DartComponentName> result, @NotNull String name, boolean lookForLValue) {
    this.result = result;
    this.name = name;
    this.isLValue = lookForLValue;
  }

  @Override
  public boolean execute(@NotNull PsiElement element, ResolveState state) {
    if (element instanceof DartComponentName) {
      final PsiElement elementParent = element.getParent();
      if (elementParent instanceof DartComponent) {
        final DartComponent dartComponent = (DartComponent)elementParent;
        // try set getter or get setter
        if (isLValue && dartComponent.isGetter()) {
          return true;
        }
        else if (!isLValue && dartComponent.isSetter()) {
          return true;
        }
      }
      final DartComponentName componentName = (DartComponentName)element;
      final DartComponentType componentType = DartComponentType.typeOf(componentName.getParent());
      if (name.equals(componentName.getText()) && !isMember(componentType)) {
        result.add(componentName);
        return false;
      }
    }
    return true;
  }

  private static boolean isMember(DartComponentType componentType) {
    return componentType == DartComponentType.CONSTRUCTOR ||
           componentType == DartComponentType.FIELD ||
           componentType == DartComponentType.METHOD;
  }

  @Override
  public <T> T getHint(@NotNull Key<T> hintKey) {
    return null;
  }

  @Override
  public void handleEvent(Event event, @Nullable Object associated) {
  }
}
