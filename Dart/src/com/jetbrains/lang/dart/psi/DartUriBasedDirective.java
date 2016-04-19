package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.NotNull;

public interface DartUriBasedDirective extends DartPsiCompositeElement {

  @NotNull
  String getUriString();

  @NotNull
  DartUriElement getUriElement();
}
