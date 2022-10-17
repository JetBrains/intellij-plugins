package com.intellij.plugins.drools.lang.psi;

import org.jetbrains.annotations.Nullable;

public interface DroolsImport extends DroolsPsiCompositeElement {

  @Nullable
  String getImportedClassName();

  @Nullable
  String getImportedPackage();

  @Nullable
  String getImportedFunction();

}
