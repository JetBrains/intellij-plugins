package com.jetbrains.lang.dart.folding;

import com.intellij.openapi.components.ServiceManager;

public abstract class DartCodeFoldingSettings {

  @SuppressWarnings({"WeakerAccess"}) public boolean COLLAPSE_GENERIC_PARAMETERS = false;

  public static DartCodeFoldingSettings getInstance() {
    return ServiceManager.getService(DartCodeFoldingSettings.class);
  }

  public abstract boolean isCollapseGenericParameters();

  public abstract void setCollapseGenericParameters(boolean value);
}
