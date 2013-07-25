package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public interface CfmlAttribute extends PsiElement {
  String getAttributeName();

  @Nullable
  String getPureAttributeValue();
}
