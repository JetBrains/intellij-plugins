package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public interface CfmlImport extends PsiElement {
  boolean isImported(String componentName);

  @Nullable
  String getImportString();

  @Nullable
  String getPrefix();
}
