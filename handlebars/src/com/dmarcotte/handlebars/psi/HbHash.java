package com.dmarcotte.handlebars.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface HbHash extends PsiElement{

  @Nullable
  String getHashName();

  @Nullable
  PsiElement getHashNameElement();
}
