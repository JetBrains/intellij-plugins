// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.jetbrains.annotations.NotNull;

public class Angular2ReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

  protected Angular2ReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters,
                           final @NotNull Processor<? super PsiReference> consumer) {
    final Angular2Pipe pipe;
    final PsiElement element = queryParameters.getElementToSearch();
    if ((pipe = Angular2EntitiesProvider.getPipe(element)) != null) {
      for (PsiElement el : pipe.getTransformMethods()) {
        if (queryParameters.getEffectiveSearchScope().contains(el.getContainingFile().getViewProvider().getVirtualFile())) {
          queryParameters.getOptimizer().searchWord(pipe.getName(), queryParameters.getEffectiveSearchScope(),
                                                    true, el);
        }
      }
    }
    else if (element instanceof TypeScriptField
             || (element instanceof TypeScriptFunction && element.getContext() instanceof JSClass)
             || (element instanceof JSParameter
                 && TypeScriptPsiUtil.isFieldParameter((JSParameter)element))) {
      String name = ((JSAttributeListOwner)element).getName();
      if (name != null && ((JSQualifiedNamedElement)element).getAccessType() == JSAttributeList.AccessType.PRIVATE) {
        Angular2Component component = Angular2EntitiesProvider.getComponent(PsiTreeUtil.getContextOfType(element, TypeScriptClass.class));
        PsiFile template;
        if (component != null && (template = component.getTemplateFile()) != null) {
          queryParameters.getOptimizer().searchWord(
            name, GlobalSearchScope.fileScope(template).intersectWith(queryParameters.getScopeDeterminedByUser()),
            false, element);
        }
      }
    }
  }
}
