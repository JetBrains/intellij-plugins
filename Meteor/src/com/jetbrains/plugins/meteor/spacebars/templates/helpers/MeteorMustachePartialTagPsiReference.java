package com.jetbrains.plugins.meteor.spacebars.templates.helpers;


import com.dmarcotte.handlebars.psi.impl.HbPsiElementImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Partial mustache template: {{> templateName}} or {{> helperName}}
 */
public class MeteorMustachePartialTagPsiReference extends MeteorMustacheTagPsiReference {

  MeteorMustachePartialTagPsiReference(HbPsiElementImpl item, String name) {
    super(item, name);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode, boolean includeWeak) {
    PsiElement item = resolveAsTemplate();

    if (item != null) {
      return new ResolveResult[]{new PsiElementResolveResult(item)};
    }

    return super.multiResolve(incompleteCode, includeWeak);
  }

  private PsiElement resolveAsTemplate() {
    ArrayList<PsiElement> declarations = MeteorTemplateIndex.findDeclarations(myName, myElement.getManager(),
                                                                              GlobalSearchScope.projectScope(myElement.getProject()));

    return ContainerUtil.getFirstItem(declarations);
  }


  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) {
    return handleElementRename(newElementName, true);
  }
}
