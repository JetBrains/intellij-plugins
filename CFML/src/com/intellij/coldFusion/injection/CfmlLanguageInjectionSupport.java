package com.intellij.coldFusion.injection;

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.coldFusion.patterns.CfmlPatterns;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport;
import org.intellij.plugins.intelliLang.inject.config.BaseInjection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Karashevich
 */
public class CfmlLanguageInjectionSupport extends AbstractLanguageInjectionSupport {

  @NonNls private static final String SUPPORT_ID = "cfml";

  @NotNull
  @Override
  public String getId() {
    return SUPPORT_ID;
  }

  @NotNull
  @Override
  public Class[] getPatternClasses() {
    return new Class[] { CfmlPatterns.class };
  }

  @Override
  public boolean isApplicableTo(PsiLanguageInjectionHost host) {
    return host instanceof CfmlTagImpl;
  }

  @Override
  public boolean useDefaultInjector(PsiLanguageInjectionHost host) {
    return true;
  }

  @Nullable
  @Override
  public String getHelpId() {
    return null;
  }

  @Nullable
  @Override
  public BaseInjection findCommentInjection(@NotNull PsiElement host, @Nullable Ref<PsiElement> commentRef) {
    return super.findCommentInjection(host, commentRef);
  }

}
