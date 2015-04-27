package org.angularjs;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagValue;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.codeInsight.DirectiveUtil;
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
      return getElementForDocumentation(element.getProject(), DirectiveUtil.normalizeAttributeName(element.getText()));
    }
    return null;
  }

  private static PsiElement getElementForDocumentation(final Project project, final String directiveName) {
    final JSImplicitElement directive = AngularIndexUtil.resolve(project, AngularDirectivesDocIndex.KEY, directiveName);
    if (directive != null) {
      final PsiComment comment = PsiTreeUtil.getParentOfType(directive, PsiComment.class);
      if (comment != null) {
        return comment;
      }
    }
    return directive;
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
          final String directiveName = DirectiveUtil.attributeToDirective(name);
          return Collections.singletonList("http://docs.angularjs.org/api/ng/directive/" + directiveName);
        }
      }
    }
    return null;
  }
}
