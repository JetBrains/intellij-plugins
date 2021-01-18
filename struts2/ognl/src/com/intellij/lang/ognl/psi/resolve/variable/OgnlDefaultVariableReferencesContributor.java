/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.ognl.psi.resolve.variable;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
final class OgnlDefaultVariableReferencesContributor extends OgnlVariableReferencesContributor {
  private static final String ORIGIN_INFO = "OGNL";

  @Override
  public boolean process(@NotNull PsiElement element,
                         @NotNull PsiFile containingFile,
                         @NotNull Processor<OgnlVariableReference> processor) {
    final Project project = containingFile.getProject();
    final PsiElement selfNavigation = element.getOriginalElement();

    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
    final PsiClass mapClass = javaPsiFacade.findClass(CommonClassNames.JAVA_UTIL_MAP, GlobalSearchScope.allScope(project));

    final OgnlVariableReference context =
      new OgnlVariableReference("context", "java.util.Map<String,Object>", ORIGIN_INFO, mapClass != null ? mapClass : selfNavigation);
    if (!processor.process(context)) {
      return false;
    }
    if (!processor.process(new OgnlVariableReference("root", CommonClassNames.JAVA_LANG_OBJECT, ORIGIN_INFO, selfNavigation))) {
      return false;
    }
    if (!processor.process(new OgnlVariableReference("this", CommonClassNames.JAVA_LANG_OBJECT, ORIGIN_INFO, selfNavigation))) {
      return false;
    }

    return true;
  }
}
