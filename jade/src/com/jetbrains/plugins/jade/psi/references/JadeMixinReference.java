package com.jetbrains.plugins.jade.psi.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import com.jetbrains.plugins.jade.psi.stubs.JadeMixinIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JadeMixinReference extends PsiPolyVariantReferenceBase<PsiNameIdentifierOwner> {


  public JadeMixinReference(PsiNameIdentifierOwner element, TextRange range) {
    super(element, range);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    String name = getCanonicalText();
    Project project = getElement().getProject();
    Collection<JadeMixinDeclarationImpl> declarations = JadeMixinIndex.find(name, project, GlobalSearchScope.allScope(project));
    return PsiElementResolveResult.createResults(declarations);
  }

  @Override
  public Object @NotNull [] getVariants() {
    Collection<String> keys = JadeMixinIndex.getKeys(getElement().getProject());
    return ArrayUtilRt.toStringArray(keys);
  }
}
