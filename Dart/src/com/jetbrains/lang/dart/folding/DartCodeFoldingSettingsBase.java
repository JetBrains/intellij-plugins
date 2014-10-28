package com.jetbrains.lang.dart.folding;

public class DartCodeFoldingSettingsBase extends DartCodeFoldingSettings {

  @Override
  public boolean isCollapseGenericParameters() {
    return COLLAPSE_GENERIC_PARAMETERS;
  }

  @Override
  public void setCollapseGenericParameters(final boolean value) {
    COLLAPSE_GENERIC_PARAMETERS = value;
  }
}
