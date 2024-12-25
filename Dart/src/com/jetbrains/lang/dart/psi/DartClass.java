// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi;

import com.jetbrains.lang.dart.util.DartClassResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DartClass extends DartComponent {

  boolean isEnum();

  @NotNull
  List<DartEnumConstantDeclaration> getEnumConstantDeclarationList();

  DartClassResolveResult getSuperClassResolvedOrObjectClass();

  @Nullable
  DartType getSuperClass();

  @NotNull
  List<DartType> getImplementsList();

  @NotNull
  List<DartType> getMixinsList();

  boolean isGeneric();

  @NotNull
  List<DartComponent> getMethods();

  @NotNull
  List<DartComponent> getFields();

  @NotNull
  List<DartComponent> getConstructors();

  @Nullable
  DartComponent findFieldByName(final @NotNull String name);

  @Nullable
  DartComponent findMethodByName(final @NotNull String name);

  @Nullable
  DartComponent findMemberByName(final @NotNull String name);

  @NotNull
  List<DartComponent> findMembersByName(final @NotNull String name);

  @Nullable
  DartTypeParameters getTypeParameters();

  @Nullable
  DartMethodDeclaration findOperator(String operator, @Nullable DartClass rightDartClass);

  List<DartMethodDeclaration> getOperators();

  @Nullable
  DartComponent findNamedConstructor(String name);
}
