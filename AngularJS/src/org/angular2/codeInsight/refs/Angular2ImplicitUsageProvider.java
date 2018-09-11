// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.angular2.lang.Angular2LangUtil;

import static org.angular2.codeInsight.refs.Angular2RefUtil.getParentClass;
import static org.angular2.codeInsight.refs.Angular2RefUtil.isPrivateMember;

public class Angular2ImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if ((element instanceof TypeScriptFunction
         || element instanceof TypeScriptField
         || element instanceof TypeScriptParameterImpl)
        && isPrivateMember((JSPsiElementBase)element)) {
      TypeScriptClass cls = getParentClass(element);
      if (cls != null && Angular2LangUtil.isAngular2Context(element)) {
        HtmlFileImpl template = Angular2RefUtil.findAngularComponentTemplate(cls);
        if (template != null) {
          return isReferencedInTemplate(element, template);
        }
      }
      return false;
    }
    return false;
  }

  private static boolean isReferencedInTemplate(PsiElement node, HtmlFileImpl template) {
    Processor<PsiReference> processor = reference ->
      reference instanceof PsiElement &&
      (JSResolveUtil.isSelfReference((PsiElement)reference) ||
       node instanceof JSFunction && PsiTreeUtil.isAncestor(node, (PsiElement)reference, false));

    SearchScope scope = new LocalSearchScope(new PsiElement[]{template}, "template", true);
    if (!ReferencesSearch.search(node, scope, true).forEach(processor)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
