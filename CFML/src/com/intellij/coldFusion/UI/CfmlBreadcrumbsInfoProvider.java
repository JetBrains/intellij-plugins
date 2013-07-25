package com.intellij.coldFusion.UI;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.breadcrumbs.BreadcrumbsInfoProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 09.02.2009
 */
public class CfmlBreadcrumbsInfoProvider extends BreadcrumbsInfoProvider {
    public Language[] getLanguages() {
        return new Language[]{CfmlLanguage.INSTANCE, StdLanguages.HTML};
    }

    public boolean acceptElement(@NotNull final PsiElement e) {
        return e instanceof CfmlTag || e instanceof XmlTag;
    }

    public PsiElement getParent(@NotNull final PsiElement e) {
        return e instanceof CfmlTag ?
                PsiTreeUtil.getParentOfType(e, CfmlTag.class) :
                PsiTreeUtil.getParentOfType(e, XmlTag.class);
    }

    @NotNull
    public String getElementInfo(@NotNull final PsiElement e) {
      String result = e instanceof CfmlTag ? ((CfmlTag) e).getTagName() :
                e instanceof XmlTag ? ((XmlTag) e).getName() : "";
        return result != null ? result : "";
    }

    public String getElementTooltip(@NotNull final PsiElement e) {
        return null;
    }
}

