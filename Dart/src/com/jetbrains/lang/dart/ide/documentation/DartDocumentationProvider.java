package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartDocumentationProvider implements DocumentationProvider {
  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    final StringBuilder builder = new StringBuilder();
    final DartComponentType type = DartComponentType.typeOf(namedComponent);
    if (namedComponent instanceof DartClass) {
      builder.append(namedComponent.getName());
    }
    else if (type == DartComponentType.FIELD || type == DartComponentType.METHOD) {
      final DartClass haxeClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class);
      assert haxeClass != null;
      builder.append(haxeClass.getName());
      builder.append(" ");
      builder.append(type.toString().toLowerCase());
      builder.append(" ");
      builder.append(namedComponent.getName());
    }
    final PsiComment comment = DartResolveUtil.findDocumentation(namedComponent);
    if (comment != null) {
      builder.append("<br/>");
      builder.append(DartPresentableUtil.unwrapCommentDelimiters(comment.getText()));
    }
    return builder.toString();
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
    return null;
  }

  @Override
  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
    return null;
  }
}
