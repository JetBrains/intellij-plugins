package com.dmarcotte.handlebars.psi;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;

/**
 * Base for all Handlebars/Mustache elements
 */
public interface HbPsiElement extends PsiElement {
  
  @NlsSafe
  String getName();
}
