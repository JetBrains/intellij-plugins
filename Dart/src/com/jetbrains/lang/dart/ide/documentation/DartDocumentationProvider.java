package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DartDocumentationProvider implements DocumentationProvider {
  private static final String BASE_DART_DOC_URL = "http://api.dartlang.org/docs/releases/latest/";
  private static final String STD_LIB_PREFIX = "dart.";

  // Scraped 08/16/2014
  private static final Set<String> APIDOC_HOSTED_PACKAGES = new THashSet<String>(Arrays.asList(
    "analyzer", "args", "barback", "code_transformers", "collection", "crypto", "csslib", "custom_element",
    "fixnum", "html5lib", "http", "http_parser", "http_server", "intl", "json_rpc_2", "logging",
    "matcher", "math", "mine", "mock", "oauth2", "observe", "path", "polymer", "polymer_expressions", "scheduled_test",
    "serialization", "shelf", "smoke", "source_maps", "stack_trace", "string_scanner", "template_binding",
    "typed_data", "unittest", "utf", "watcher", "web_components", "yaml"
  ));

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
    else if (APIDOC_HOSTED_PACKAGES.contains(libName)){
      resultUrl.append(libName);
    }
    else {
      return null;
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
