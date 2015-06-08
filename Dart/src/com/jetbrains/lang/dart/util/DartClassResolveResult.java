package com.jetbrains.lang.dart.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartClassResolveResult implements Cloneable {
  public static final DartClassResolveResult EMPTY = new DartClassResolveResult(null);

  private final @Nullable DartClass myDartClass;
  private final @NotNull DartGenericSpecialization mySpecialization;

  protected DartClassResolveResult(@Nullable final DartClass aClass) {
    this(aClass, new DartGenericSpecialization());
  }

  protected DartClassResolveResult(@Nullable final DartClass aClass, @NotNull final DartGenericSpecialization specialization) {
    myDartClass = aClass;
    mySpecialization = specialization;
  }

  @Override
  public DartClassResolveResult clone() {
    return new DartClassResolveResult(myDartClass, mySpecialization.clone());
  }

  @NotNull
  public static DartClassResolveResult create(@Nullable final DartClass dartClass) {
    return create(dartClass, new DartGenericSpecialization());
  }

  @NotNull
  public static DartClassResolveResult create(@Nullable final DartClass dartClass,
                                              @NotNull final DartGenericSpecialization specialization) {
    if (dartClass == null) {
      return new DartClassResolveResult(null);
    }

    DartClassResolveResult resolveResult = DartClassResolveCache.getInstance(dartClass.getProject()).get(dartClass);

    if (resolveResult == null) {
      resolveResult = new DartClassResolveResult(dartClass);
      DartClassResolveCache.getInstance(dartClass.getProject()).put(dartClass, resolveResult);

      final DartType superClass = dartClass.getSuperClass();
      if (superClass != null) {
        final DartClassResolveResult result = DartResolveUtil.resolveClassByType(superClass);
        result.specializeByParameters(superClass.getTypeArguments());
        resolveResult.merge(result.getSpecialization());
      }
      for (DartType dartType : DartResolveUtil.getImplementsAndMixinsList(dartClass)) {
        final DartClassResolveResult result = DartResolveUtil.resolveClassByType(dartType);
        result.specializeByParameters(dartType.getTypeArguments());
        resolveResult.merge(result.getSpecialization());
      }
    }

    final DartClassResolveResult clone = resolveResult.clone();
    clone.softMerge(specialization);
    return clone;
  }

  private void merge(@NotNull final DartGenericSpecialization otherSpecializations) {
    for (String key : otherSpecializations.map.keySet()) {
      mySpecialization.map.put(key, otherSpecializations.map.get(key));
    }
  }

  private void softMerge(@NotNull final DartGenericSpecialization otherSpecializations) {
    for (String key : otherSpecializations.map.keySet()) {
      if (!mySpecialization.map.containsKey(key)) {
        mySpecialization.map.put(key, otherSpecializations.map.get(key));
      }
    }
  }

  @Nullable
  public DartClass getDartClass() {
    return myDartClass;
  }

  @NotNull
  public DartGenericSpecialization getSpecialization() {
    return mySpecialization;
  }

  public void specialize(@Nullable final PsiElement element) {
    if (element == null || myDartClass == null || !myDartClass.isGeneric()) {
      return;
    }
    if (element instanceof DartNewExpression) {
      final DartType type = ((DartNewExpression)element).getType();
      if (type != null) {
        specializeByParameters(type.getTypeArguments());
      }
    }
  }

  public void specializeByParameters(@Nullable final DartTypeArguments typeArguments) {
    if (typeArguments == null || myDartClass == null || !myDartClass.isGeneric()) {
      return;
    }
    final DartTypeParameters parameters = myDartClass.getTypeParameters();
    assert parameters != null;
    final List<DartType> typeList = typeArguments.getTypeList().getTypeList();
    for (int i = 0, size = parameters.getTypeParameterList().size(); i < size; i++) {
      DartTypeParameter dartTypeParameter = parameters.getTypeParameterList().get(i);
      DartComponentName componentName = dartTypeParameter == null ? null : dartTypeParameter.getComponentName();
      final DartType specializedType = typeList.get(i);
      if (componentName == null || specializedType == null) continue;
      mySpecialization.put(myDartClass, componentName.getText(), DartResolveUtil.getDartClassResolveResult(specializedType,
                                                                                                           mySpecialization));
    }

    specializeSupers(myDartClass, mySpecialization);
  }

  private static void specializeSupers(@Nullable final DartClass dartClass, @NotNull final DartGenericSpecialization specialization) {
    if (dartClass == null) {
      return;
    }

    final DartType superType = dartClass.getSuperClass();
    if (superType != null) {
      specializeSuperType(dartClass, specialization, superType);
    }
    for (DartType dartType : DartResolveUtil.getImplementsAndMixinsList(dartClass)) {
      specializeSuperType(dartClass, specialization, dartType);
    }
  }

  private static void specializeSuperType(@Nullable final DartClass dartClass,
                                          @NotNull final DartGenericSpecialization specialization,
                                          @NotNull final DartType type) {
    final DartTypeArguments targetTypeArguments = type.getTypeArguments();
    if (targetTypeArguments == null || dartClass == null || dartClass.getTypeParameters() == null) {
      return;
    }
    final DartClassResolveResult typeTargetClassResolve = DartResolveUtil.resolveClassByType(type);
    final DartClass typeTargetClass = typeTargetClassResolve.getDartClass();
    if (typeTargetClass == null) {
      return;
    }
    final DartTypeParameters typeTargetClassTypeParameters = typeTargetClass.getTypeParameters();
    List<DartType> list = targetTypeArguments.getTypeList().getTypeList();
    if (typeTargetClassTypeParameters == null || typeTargetClassTypeParameters.getTypeParameterList().size() != list.size()) {
      return;
    }
    for (int i = 0, size = list.size(); i < size; i++) {
      final DartType argumentType = list.get(i);
      final PsiElement argumentTarget = argumentType.resolveReference();
      if (argumentTarget == null ||
          dartClass.getTypeParameters() != PsiTreeUtil.getParentOfType(argumentTarget, DartTypeParameters.class)) {
        // argumentTarget isn't our generic
        continue;
      }
      final String superGenericName = typeTargetClassTypeParameters.getTypeParameterList().get(i).getComponentName().getText();

      final DartClassResolveResult resolveResult = specialization.get(typeTargetClass, superGenericName);
      if (resolveResult == null || resolveResult.getDartClass() == null) {
        specialization.put(typeTargetClass, superGenericName, specialization.get(dartClass, argumentTarget.getText()));
        specializeSupers(typeTargetClass, specialization);
      }
    }
  }
}
