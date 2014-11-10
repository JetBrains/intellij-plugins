package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DartEnum extends DartComponent {

  @NotNull
  List<DartEnumConstantDeclaration> getConstants();

}
