package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartDocumentationProvider implements DocumentationProvider {
  private static final String BASE_DART_DOC_URL = "http://api.dartlang.org/docs/releases/latest/";
  private static final String STD_LIB_PREFIX = "dart.";

  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    final String componentName = namedComponent.getName();
    if (componentName == null || !namedComponent.isPublic()) {
      return null;
    }

    final List<String> result = new ArrayList<String>();

    final PsiManager psiManager = element.getManager();
    List<VirtualFile> library = DartResolveUtil.findLibrary(element.getContainingFile());
    for (VirtualFile libraryRoot : library) {
      final PsiFile libPsiFile = psiManager.findFile(libraryRoot);
      String libName = libPsiFile != null ? DartResolveUtil.getLibraryName(libPsiFile) : null;
      final String docUrl = libName != null ? constructDocUrl(namedComponent, componentName, libName) : null;
      if (docUrl != null) {
        result.add(docUrl);
      }
    }

    return result;
  }

  @Nls
  private static String constructDocUrl(DartComponent namedComponent, String componentName, @NotNull String libName) {
    // class:     http://api.dartlang.org/docs/releases/latest/args/ArgParser.html
    // method:    http://api.dartlang.org/docs/releases/latest/args/ArgParser.html#addCommand
    // function:  http://api.dartlang.org/docs/releases/latest/observe.html#toObservable

    final StringBuilder resultUrl = new StringBuilder(BASE_DART_DOC_URL);
    if (libName.startsWith(STD_LIB_PREFIX)) {
      resultUrl.append("dart_").append(libName.substring(STD_LIB_PREFIX.length()));
    }
    else {
      resultUrl.append(libName);
    }

    final DartClass dartClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class, true);
    final DartComponentType componentType = DartComponentType.typeOf(namedComponent);

    if (dartClass != null) {
      // method
      resultUrl.append('/').append(dartClass.getName()).append(".html").append('#').append(componentName);
    }
    else if (componentType == DartComponentType.CLASS) {
      // class
      resultUrl.append('/').append(componentName).append(".html");
    }
    else {
      // function
      resultUrl.append(".html").append('#').append(componentName);
    }

    return resultUrl.toString();
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
