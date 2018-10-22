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

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
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
                           @NotNull final Processor<? super PsiReference> consumer) {
    final JSImplicitElement directive;
    final Angular2Pipe pipe;
    final Angular2Component component;
    final PsiElement element = queryParameters.getElementToSearch();
    if ((directive = DirectiveUtil.getDirective(element)) != null) {
      queryParameters.getOptimizer().searchWord(directive.getName(), queryParameters.getEffectiveSearchScope(),
                                                true, directive);
    }
    else if ((component = Angular2EntitiesProvider.getComponent(element)) != null) {
      queryParameters.getOptimizer().searchWord(component.getSelector(), queryParameters.getEffectiveSearchScope(),
                                                true, component.getSourceElement());
    }
    else if ((pipe = Angular2EntitiesProvider.getPipe(element)) != null) {
      if (pipe.getTransformMethods() != null) {
        for (PsiElement el : pipe.getTransformMethods()) {
          queryParameters.getOptimizer().searchWord(pipe.getName(), queryParameters.getEffectiveSearchScope(),
                                                    true, el);
        }
      }
    }
  }
}
