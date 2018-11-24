package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.Nullable;

public interface IDartBlock extends DartPsiCompositeElement {

  @Nullable
  DartStatements getStatements();
}
