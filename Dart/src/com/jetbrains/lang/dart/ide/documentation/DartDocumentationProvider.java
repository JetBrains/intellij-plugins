package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.dartlang.analysis.server.protocol.HoverInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartDocumentationProvider implements DocumentationProvider {
  private static final String BASE_DART_DOC_URL = "https://api.dartlang.org/stable/";

  @Override
  public String generateDoc(@NotNull final PsiElement element, @Nullable final PsiElement originalElement) {
    // in case of code completion 'element' comes from completion list and has nothing to do with 'originalElement',
    // but for Quick Doc in editor we should prefer building docs for 'originalElement' because such doc has info about propagated type
    final PsiElement elementForDocs = resolvesTo(originalElement, element) ? originalElement : element;
    final HoverInformation hover = getSingleHover(elementForDocs);
    if (hover != null) {
      return generateDocServer(hover);
    }
    return DartDocUtil.generateDoc(element);
  }

  private static boolean resolvesTo(@Nullable final PsiElement originalElement, @NotNull final PsiElement target) {
    final PsiReference reference;

    if (originalElement instanceof PsiReference) {
      reference = (PsiReference)originalElement;
    }
    else {
      final PsiElement parent = originalElement == null ? null : originalElement.getParent();
      final PsiElement parentParent = parent instanceof DartId ? parent.getParent() : null;
      if (parentParent == null) return false;
      if (parentParent == target) return true;
      if (!parentParent.getText().equals(target.getText())) return false;
      reference = parentParent.getReference();
    }

    return reference != null && reference.resolve() == target;
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
  public String getQuickNavigateInfo(final PsiElement element, final PsiElement originalElement) {
    final PsiElement elementForInfo = resolvesTo(originalElement, element) ? originalElement : element;
    final HoverInformation hover = getSingleHover(elementForInfo);
    if (hover != null) {
      return buildHoverTextServer(hover);
    }
    return DartDocUtil.getSignature(element);
  }

  @Override
  @Nullable
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }

    final DartComponent component = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    if (!component.isPublic()) return null;

    final String docUrl = constructDocUrl(component);
    return docUrl == null ? null : Collections.singletonList(docUrl);
  }

  @NotNull
  public static String buildHoverTextServer(@NotNull final HoverInformation hover) {
    final String elementDescription = hover.getElementDescription();
    final String staticType = elementDescription == null || elementDescription.equals(hover.getStaticType()) ? null : hover.getStaticType();
    final String propagatedType =
      elementDescription == null || elementDescription.equals(hover.getPropagatedType()) ? null : hover.getPropagatedType();
    return DartDocUtil.generateDoc(elementDescription, false, null, null, null, staticType, propagatedType, true);
  }

  @NotNull
  public static String generateDocServer(@NotNull final HoverInformation hover) {
    final String elementDescription = hover.getElementDescription();
    final String containingLibraryName = hover.getContainingLibraryName();
    final String containingClassDescription = hover.getContainingClassDescription();
    final String staticType = hover.getStaticType();
    final String propagatedType = hover.getPropagatedType();
    final String docText = hover.getDartdoc();
    return DartDocUtil.generateDoc(elementDescription, false, docText, containingLibraryName, containingClassDescription,
                                   staticType, propagatedType, false);
  }

  @Nullable
  public static HoverInformation getSingleHover(@NotNull final PsiFile psiFile, final int offset) {
    final List<HoverInformation> hoverList =
      DartAnalysisServerService.getInstance(psiFile.getProject()).analysis_getHover(psiFile.getVirtualFile(), offset);
    if (hoverList.isEmpty()) {
      return null;
    }
    return hoverList.get(0);
  }

  @Nullable
  private static String constructDocUrl(@NotNull final DartComponent component) {
    // class:       https://api.dartlang.org/stable/dart-web_audio/AnalyserNode-class.html
    // constructor: https://api.dartlang.org/stable/dart-core/DateTime/DateTime.fromMicrosecondsSinceEpoch.html
    //              https://api.dartlang.org/stable/dart-core/List/List.html
    // method:      https://api.dartlang.org/stable/dart-core/Object/toString.html
    // property:    https://api.dartlang.org/stable/dart-core/List/length.html
    // function:    https://api.dartlang.org/stable/dart-math/cos.html

    final String libRelatedUrlPart = getLibRelatedUrlPart(component);
    final String name = component.getName();
    if (libRelatedUrlPart == null || name == null) return null;

    final String baseUrl = BASE_DART_DOC_URL + libRelatedUrlPart + "/";

    if (component instanceof DartClass) {
      return baseUrl + name + "-class.html";
    }

    final DartClass dartClass = PsiTreeUtil.getParentOfType(component, DartClass.class, true);

    if (component instanceof DartNamedConstructorDeclaration) {
      assert dartClass != null;
      return baseUrl + dartClass.getName() + "/" +
             StringUtil.join(((DartNamedConstructorDeclaration)component).getComponentNameList(), NavigationItem::getName, ".") +
             ".html";
    }

    if (component instanceof DartFactoryConstructorDeclaration) {
      assert dartClass != null;
      return baseUrl + dartClass.getName() + "/" +
             StringUtil.join(((DartFactoryConstructorDeclaration)component).getComponentNameList(), NavigationItem::getName, ".") +
             ".html";
    }

    if (dartClass != null) {
      // method, property
      return baseUrl + dartClass.getName() + "/" + name + ".html";
    }
    else {
      // library-level function
      return baseUrl + name + ".html";
    }
  }

  @Nullable
  private static String getLibRelatedUrlPart(@NotNull final PsiElement element) {
    for (VirtualFile libFile : DartResolveUtil.findLibrary(element.getContainingFile())) {
      final DartUrlResolver urlResolver = DartUrlResolver.getInstance(element.getProject(), libFile);

      final String dartUrl = urlResolver.getDartUrlForFile(libFile);
      // "dart:html" -> "dart-html"
      if (dartUrl.startsWith(DartUrlResolver.DART_PREFIX)) {
        return "dart-" + dartUrl.substring(DartUrlResolver.DART_PREFIX.length());
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
