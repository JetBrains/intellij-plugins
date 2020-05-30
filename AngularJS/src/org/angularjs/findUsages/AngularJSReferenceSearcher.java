// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angularjs.findUsages;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.xml.util.HtmlUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

public class AngularJSReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  protected AngularJSReferenceSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters,
                           final @NotNull Processor<? super PsiReference> consumer) {
    final JSImplicitElement directive;
    final Angular2Pipe pipe;
    final PsiElement element = queryParameters.getElementToSearch();
    if ((directive = DirectiveUtil.getDirective(element)) != null) {
      for (String attrName : DirectiveUtil.getAttributeNameVariations(directive.getName())) {
        queryParameters.getOptimizer().searchWord(attrName, queryParameters.getEffectiveSearchScope(), false, directive);
        queryParameters.getOptimizer().searchWord("x-" + attrName, queryParameters.getEffectiveSearchScope(), false, directive);
        queryParameters.getOptimizer()
          .searchWord(HtmlUtil.HTML5_DATA_ATTR_PREFIX + attrName, queryParameters.getEffectiveSearchScope(), false, directive);
      }
    }
    else if ((pipe = Angular2EntitiesProvider.getPipe(element)) != null) {
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
          queryParameters.getOptimizer().searchWord(name, GlobalSearchScope.fileScope(template), false, element);
        }
      }
    }
  }
}
