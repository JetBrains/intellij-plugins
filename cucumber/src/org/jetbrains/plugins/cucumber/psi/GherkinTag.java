package org.jetbrains.plugins.cucumber.psi;

public interface GherkinTag extends GherkinPsiElement {
  GherkinTag[] EMPTY_ARRAY = new GherkinTag[0];

  public String getName();
}
