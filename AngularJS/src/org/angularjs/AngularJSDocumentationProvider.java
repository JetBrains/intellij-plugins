// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlElement;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDocumentationProvider extends DocumentationProviderEx {

  private static PsiElement getElementForDocumentation(final Project project, final String directiveName) {
    return AngularIndexUtil.resolve(project, AngularDirectivesDocIndex.KEY, directiveName);
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(@NotNull PsiManager psiManager, @NotNull Object object, @NotNull PsiElement element) {
    if (element instanceof XmlElement) {
      return getElementForDocumentation(element.getProject(), object.toString());
    }
    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (element instanceof JSImplicitElement) element = element.getParent();
    if (element instanceof JSDocComment) {
      JSDocTag ngdocTag = null;
      JSDocTag nameTag = null;
      for (JSDocTag tag : ((JSDocComment)element).getTags()) {
        if ("ngdoc".equals(tag.getName())) ngdocTag = tag;
        else if ("name".equals(tag.getName())) nameTag = tag;
      }
      if (ngdocTag != null && nameTag != null) {
        final JSDocTagValue nameValue = nameTag.getValue();
        String name = nameValue != null ? nameValue.getText() : null;
        if (name != null) name = name.substring(name.indexOf(':') + 1);

        if (name != null && AngularIndexUtil.resolve(element.getProject(), AngularDirectivesDocIndex.KEY, DirectiveUtil.getAttributeName(name)) != null) {
          final String directiveName = DirectiveUtil.attributeToDirective(null, name);
          return Collections.singletonList("https://docs.angularjs.org/api/ng/directive/" + directiveName);
        }
      }
    }
    return null;
  }
}
