// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.impl.JSDocTags;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlElement;
import org.angularjs.index.AngularJSIndexingHandler;

import java.util.Collections;
import java.util.List;

import static org.angularjs.index.AngularJSDirectivesSupport.findDirective;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDocumentationProvider implements DocumentationProvider {

  private static PsiElement getElementForDocumentation(final Project project, final String directiveName) {
    JSImplicitElement directive = findDirective(project, directiveName);
    return directive != null
           && AngularJSIndexingHandler.ANGULAR_DIRECTIVES_DOC_INDEX_USER_STRING.equals(directive.getUserString())
           ? directive : null;
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
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
        if (tag.is("ngdoc")) {
          ngdocTag = tag;
        }
        else if (tag.is(JSDocTags.NAME)) {
          nameTag = tag;
        }
      }
      if (ngdocTag != null && nameTag != null) {
        String name = nameTag.getNamepathText();
        if (name != null) name = name.substring(name.indexOf(':') + 1);
        if (name != null && getElementForDocumentation(element.getProject(), name) != null) {
          return Collections.singletonList("https://docs.angularjs.org/api/ng/directive/" + name);
        }
      }
    }
    return null;
  }
}
