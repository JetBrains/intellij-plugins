package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
<<<<<<< HEAD
import gnu.trove.THashSet;
import org.apache.commons.lang.StringUtils;
=======
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.apache.commons.lang3.StringUtils;
>>>>>>> 7a6685d... Dart documentation URL fixes.
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
    final String docUrl = constructDocUrl(namedComponent, componentName, libraryName);
    return docUrl == null ? null : Collections.singletonList(docUrl);
  }

  private String getLibraryName(@NotNull final PsiElement element) {
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
    if (virtualFile == null) {
      return null;
    }

    final DartUrlResolver urlResolver =
      DartUrlResolver.getInstance(element.getProject(), virtualFile);

    final String dartUrl = urlResolver.getDartUrlForFile(virtualFile);
    final String prefix = StringUtils.substringBefore(dartUrl, "/");
    // "dart:html" -> "dart_html"
    if (prefix.startsWith("dart:")) {
      return prefix.replaceFirst(":", "_");
    }
    // "package:unittest" -> "unittest"
    if (prefix.startsWith("package:")) {
      return prefix.substring(8);
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

<<<<<<< HEAD
  @VisibleForTesting
  public static StringBuilder constructDocUrlPrefix(String libName) {
    final StringBuilder resultUrl = new StringBuilder(BASE_DART_DOC_URL);
    if (libName.startsWith(STD_LIB_PREFIX)) {
      final String toReplace = libName.startsWith(DOM_LIB_PREFIX) ? DOM_LIB_PREFIX : STD_LIB_PREFIX;
      resultUrl.append("dart_").append(libName.substring(toReplace.length()));
    } else {
      libName = StringUtils.substringBefore(libName, ".");
      if (APIDOC_HOSTED_PACKAGES.contains(libName)) {
        resultUrl.append(libName);
      }
      else {
        return null;
      }
    }
    return resultUrl;
  }

=======
>>>>>>> 7a6685d... Dart documentation URL fixes.
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
