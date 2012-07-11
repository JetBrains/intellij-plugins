package org.jetbrains.plugins.cucumber.psi;

/**
 * @author yole
 */
public interface GherkinExamplesBlock extends GherkinPsiElement {
  GherkinTable getTable();
}
