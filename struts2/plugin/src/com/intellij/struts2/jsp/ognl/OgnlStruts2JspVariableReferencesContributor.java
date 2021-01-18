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
package com.intellij.struts2.jsp.ognl;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.ognl.psi.resolve.variable.OgnlVariableReference;
import com.intellij.lang.ognl.psi.resolve.variable.OgnlVariableReferencesContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.jsp.JspFile;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * OGNL variables in JSP context provided by S2.
 *
 * @author Yann C&eacute;bron
 */
final class OgnlStruts2JspVariableReferencesContributor extends OgnlVariableReferencesContributor {
  private static final String ORIGIN_INFO = "JSP";
  private static final String[] VAR_NAMES = {"parameters", "request", "session", "application", "attr"};

  @Override
  public boolean process(@NotNull PsiElement element,
                         @NotNull PsiFile containingFile,
                         @NotNull Processor<OgnlVariableReference> processor) {
    final PsiFile topLevelFile = InjectedLanguageManager.getInstance(containingFile.getProject()).getTopLevelFile(element);
    if (!(topLevelFile instanceof JspFile)) {
      return true;
    }

    if (StrutsFacet.getInstance(element) == null) {
      return true;
    }

    final PsiElement selfNavigation = element.getOriginalElement();
    for (String name : VAR_NAMES) {
      if (!processor.process(new OgnlVariableReference(name, "java.util.Map<String,Object>", ORIGIN_INFO, selfNavigation))) {
        return false;
      }
    }

    return true;
  }
}
