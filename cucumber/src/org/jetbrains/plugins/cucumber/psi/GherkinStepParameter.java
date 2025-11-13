package org.jetbrains.plugins.cucumber.psi;

import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public interface GherkinStepParameter extends GherkinPsiElement {
  String getName();
}
