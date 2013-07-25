package com.intellij.coldFusion.model.psi;

import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 * @date 09.02.11
 */
public interface CfmlProperty extends CfmlPsiElement, CfmlVariable {
  CfmlProperty[] EMPTY_ARRAY = new CfmlProperty[0];

  boolean hasGetter();
  boolean hasSetter();

  @Nullable
  CfmlComponent getComponent();
}
