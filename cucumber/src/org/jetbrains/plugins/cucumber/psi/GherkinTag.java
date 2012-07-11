package org.jetbrains.plugins.cucumber.psi;

/**
 * User: Andrey Vokin
 * Date: 11/28/11
 */
public interface GherkinTag extends GherkinPsiElement {
  GherkinTag[] EMPTY_ARRAY = new GherkinTag[0];

  public String getName();
}
