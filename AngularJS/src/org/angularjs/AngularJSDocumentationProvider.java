package org.angularjs;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.javascript.index.AngularDirectivesIndex;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.angularjs.index.AngularIndexUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDocumentationProvider extends DocumentationProviderEx {
  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (element instanceof JSNamedElementProxy) {
      final String name = ((JSNamedElementProxy)element).getName();
      if (AngularIndexUtil.resolve(element.getProject(), AngularDirectivesIndex.INDEX_ID, name) != null) {
        final String[] words = name.split("-");
        for (int i = 1; i < words.length; i++) {
          words[i] = StringUtil.capitalize(words[i]);
        }
        return Collections.singletonList("http://docs.angularjs.org/api/ng.directive:" + StringUtil.join(words));
      }
    }
    return null;
  }
}
