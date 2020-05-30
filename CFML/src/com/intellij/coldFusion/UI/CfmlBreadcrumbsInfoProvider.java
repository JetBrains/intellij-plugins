// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlBreadcrumbsInfoProvider implements BreadcrumbsProvider {
  @Override
  public Language[] getLanguages() {
    return new Language[]{CfmlLanguage.INSTANCE, HTMLLanguage.INSTANCE};
  }

  @Override
  public boolean acceptElement(@NotNull final PsiElement e) {
    return e instanceof CfmlTag || e instanceof XmlTag;
  }

  @Override
  public PsiElement getParent(@NotNull final PsiElement e) {
    return e instanceof CfmlTag ?
           PsiTreeUtil.getParentOfType(e, CfmlTag.class) :
           PsiTreeUtil.getParentOfType(e, XmlTag.class);
  }

  @Override
  @NotNull
  public String getElementInfo(@NotNull final PsiElement e) {
    String result = e instanceof CfmlTag ? ((CfmlTag)e).getTagName() :
                    e instanceof XmlTag ? ((XmlTag)e).getName() : "";
    return result != null ? result : "";
  }

  @Override
  public String getElementTooltip(@NotNull final PsiElement e) {
    return null;
  }
}

