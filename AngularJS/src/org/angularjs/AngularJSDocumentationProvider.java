package org.angularjs;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDocumentationProvider extends DocumentationProviderEx {
  @Nullable
  @Override
  public PsiElement getCustomDocumentationElement(@NotNull Editor editor,
                                                  @NotNull PsiFile file,
                                                  @Nullable PsiElement element) {
    final IElementType elementType = element != null ? element.getNode().getElementType() : null;
    if (elementType == XmlTokenType.XML_NAME || elementType == XmlTokenType.XML_TAG_NAME) {
      return AngularIndexUtil.resolve(element.getProject(), AngularDirectivesDocIndex.INDEX_ID, element.getText());
    }
    return null;
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
    if (element instanceof XmlAttribute) {
      return AngularIndexUtil.resolve(element.getProject(), AngularDirectivesDocIndex.INDEX_ID, object.toString());
    }
    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (element instanceof JSNamedElementProxy) {
      final String name = ((JSNamedElementProxy)element).getName();
      if (AngularIndexUtil.resolve(element.getProject(), AngularDirectivesDocIndex.INDEX_ID, name) != null) {
        final String[] words = name.split("-");
        for (int i = 1; i < words.length; i++) {
          words[i] = StringUtil.capitalize(words[i]);
        }
        return Collections.singletonList("http://docs.angularjs.org/api/ng/directive/" + StringUtil.join(words));
      }
    }
    return null;
  }
}
