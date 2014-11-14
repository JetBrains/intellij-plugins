package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartSetterDeclaration;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartDocumentationProvider implements DocumentationProvider {
  private static final String BASE_DART_DOC_URL = "http://api.dartlang.org/docs/releases/latest/";

  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  @Nullable
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    final String componentName = namedComponent.getName();
    if (componentName == null || !namedComponent.isPublic()) {
      return null;
    }

    final String libraryName = getLibraryName(element);
    final String docUrl = libraryName == null ? null : constructDocUrl(namedComponent, componentName, libraryName);
    return docUrl == null ? null : Collections.singletonList(docUrl);
  }

  @Nullable
  private static String getLibraryName(@NotNull final PsiElement element) {
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
    if (virtualFile == null) {
      return null;
    }

    final DartUrlResolver urlResolver =
      DartUrlResolver.getInstance(element.getProject(), virtualFile);

    final String dartUrl = urlResolver.getDartUrlForFile(virtualFile);
    final String prefix = StringUtils.substringBefore(dartUrl, "/");
    // "dart:html" -> "dart_html"
    if (prefix.startsWith(DartUrlResolver.DART_PREFIX)) {
      return "dart_" + prefix.substring(DartUrlResolver.DART_PREFIX.length());
    }
    // "package:unittest" -> "unittest"
    if (prefix.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
      return prefix.substring(DartUrlResolver.PACKAGE_PREFIX.length());
    }

    return null;
  }

  @Nls
  private static String constructDocUrl(DartComponent namedComponent, String componentName, @NotNull String libName) {
    // class:     http://api.dartlang.org/docs/releases/latest/dart_core/Object.html
    // method:    http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_toString
    // property:  http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_hashCode
    // function:  http://api.dartlang.org/docs/releases/latest/dart_math.html#id_cos


    final StringBuilder resultUrl = new StringBuilder(BASE_DART_DOC_URL).append(libName);

    final DartClass dartClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class, true);
    final DartComponentType componentType = DartComponentType.typeOf(namedComponent);

    if (dartClass != null) {
      // method
      resultUrl.append('/').append(dartClass.getName()).append(".html#id_").append(componentName);
      if (namedComponent instanceof DartSetterDeclaration) {
        resultUrl.append('=');
      }
    }
    else if (componentType == DartComponentType.CLASS) {
      // class
      resultUrl.append('/').append(componentName).append(".html");
    }
    else {
      // function
      resultUrl.append(".html#id_").append(componentName);
    }

    return resultUrl.toString();
  }

  @Override
  public String generateDoc(PsiElement element, PsiElement originalElement) {
    return DartDocUtil.generateDoc(element);
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
