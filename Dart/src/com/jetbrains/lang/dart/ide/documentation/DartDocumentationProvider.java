package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartSetterDeclaration;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.dartlang.analysis.server.protocol.HoverInformation;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartDocumentationProvider implements DocumentationProvider {
  private static final String BASE_DART_DOC_URL = "http://api.dartlang.org/docs/releases/latest/";

  @Override
  public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
    final String serverDoc = generateDocServer(element);
    if (serverDoc != null) {
      return serverDoc;
    }
    return DartDocUtil.generateDoc(element);
  }

  @Override
  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
    return null;
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
    return null;
  }

  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return DartDocUtil.getSignature(element);
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

    final String libRelatedUrlPart = getLibRelatedUrlPart(element);
    final String docUrl = libRelatedUrlPart == null ? null : constructDocUrl(namedComponent, componentName, libRelatedUrlPart);
    return docUrl == null ? null : Collections.singletonList(docUrl);
  }

  @Nullable
  public static String generateDocServer(HoverInformation hover) {
    if (hover == null) {
      return null;
    }
    // prepare data
    final String elementDescription = hover.getElementDescription();
    final String containingLibraryName = hover.getContainingLibraryName();
    final String containingClassDescription = hover.getContainingClassDescription();
    final String staticType = hover.getStaticType();
    final String propagatedType = hover.getPropagatedType();
    final String docText = hover.getDartdoc();
    return DartDocUtil
      .generateDoc(elementDescription, false, docText, containingLibraryName, containingClassDescription, staticType, propagatedType);
  }

  @Nullable
  public static HoverInformation getSingleHover(@NotNull final PsiFile psiFile, final int offset) {
    final String filePath = psiFile.getVirtualFile().getPath();
    final List<HoverInformation> hoverList = DartAnalysisServerService.getInstance().analysis_getHover(filePath, offset);
    if (hoverList.isEmpty()) {
      return null;
    }
    return hoverList.get(0);
  }

  @Nls
  private static String constructDocUrl(DartComponent namedComponent, String componentName, @NotNull String libRelatedUrlPart) {
    // class:     http://api.dartlang.org/docs/releases/latest/dart_core/Object.html
    // method:    http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_toString
    // property:  http://api.dartlang.org/docs/releases/latest/dart_core/Object.html#id_hashCode
    // function:  http://api.dartlang.org/docs/releases/latest/dart_math.html#id_cos


    final StringBuilder resultUrl = new StringBuilder(BASE_DART_DOC_URL).append(libRelatedUrlPart);

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

  @Nullable
  private static String generateDocServer(PsiElement element) {
    final HoverInformation hover = getSingleHover(element);
    return generateDocServer(hover);
  }

  @Nullable
  private static String getLibRelatedUrlPart(@NotNull final PsiElement element) {
    for (VirtualFile libFile : DartResolveUtil.findLibrary(element.getContainingFile())) {
      final DartUrlResolver urlResolver = DartUrlResolver.getInstance(element.getProject(), libFile);

      final String dartUrl = urlResolver.getDartUrlForFile(libFile);
      // "dart:html" -> "dart_html"
      if (dartUrl.startsWith(DartUrlResolver.DART_PREFIX)) {
        return "dart_" + dartUrl.substring(DartUrlResolver.DART_PREFIX.length());
      }
      // "package:unittest" -> "unittest"
      if (dartUrl.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
        return dartUrl.substring(DartUrlResolver.PACKAGE_PREFIX.length());
      }
    }

    return null;
  }

  @Nullable
  private static HoverInformation getSingleHover(final PsiElement element) {
    if (element != null) {
      final PsiFile psiFile = element.getContainingFile();
      final int offset = element.getTextOffset();
      return getSingleHover(psiFile, offset);
    }
    return null;
  }
}
