package com.intellij.plugins.drools.lang.psi;

import org.jetbrains.annotations.NotNull;

public interface DroolsSimpleAttribute extends DroolsPsiCompositeElement {
  @NotNull
  String getAttributeName();
}
