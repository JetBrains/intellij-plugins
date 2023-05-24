// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi;

import org.jetbrains.annotations.Nullable;

public interface DroolsImport extends DroolsPsiCompositeElement {

  @Nullable
  String getImportedClassName();

  @Nullable
  String getImportedPackage();

  @Nullable
  String getImportedFunction();

  boolean isFunction();
}
