/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.util.List;

/**
 * Created by Lera Nikolaenko
 * Date: 27.10.2008
 */
public class CfmlDocumentProvider implements DocumentationProvider {
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof CfmlAttributeImpl && element.getParent() instanceof CfmlTag) {
      return CfmlUtil.getAttributeDescription(((CfmlTag)element.getParent()).getTagName().toLowerCase(),
                                              ((CfmlAttributeImpl)element).getName().toLowerCase(), element.getProject());
    }
    else if (element instanceof CfmlTag) {
      String name = ((CfmlTag)element).getTagName().toLowerCase();
      if (CfmlUtil.isStandardTag(name, element.getProject())) {
        return CfmlUtil.getTagDescription(name, element.getProject());
      }
    }
    return "No documentation provided for " +
           element.getText();  //To change body of implemented methods use File | Settings | File Templates.
  }

  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
