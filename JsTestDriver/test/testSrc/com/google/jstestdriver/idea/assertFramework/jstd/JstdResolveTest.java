package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.ResolveTestCase;
import com.intellij.util.ArrayUtil;
import com.intellij.webcore.libraries.ScriptingLibraryManager;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Sergey Simonchik
 */
public class JstdResolveTest extends ResolveTestCase {

  private static final boolean ADD_LIBRARY = true;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (ADD_LIBRARY) {
      Collection<VirtualFile> jstdLibSourceFiles = VfsUtils.findVirtualFilesByResourceNames(
        JstdDefaultAssertionFrameworkSrcMarker.class,
        new String[]{"Asserts.js", "TestCase.js"}
      );
      addJstdLibrary(getProject(), jstdLibSourceFiles);
    }
  }

  @Override
  public void tearDown() throws Exception {
    if (ADD_LIBRARY) {
      removeLibrary(getProject());
    }
    super.tearDown();
  }

  private static void removeLibrary(@NotNull final Project project) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ScriptingLibraryManager libraryManager = ServiceManager.getService(project, JSLibraryManager.class);
        ScriptingLibraryModel model = libraryManager.getLibraryByName(JstdLibraryUtil.LIBRARY_NAME);
        assert model != null;
        libraryManager.removeLibrary(model);
        libraryManager.commitChanges();
      }
    });
  }

  private static void addJstdLibrary(@NotNull final Project project,
                                     @NotNull final Collection<VirtualFile> libSourceFiles) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {

      @Override
      public void run() {
        JSLibraryManager jsLibraryManager = ServiceManager.getService(project, JSLibraryManager.class);
        ScriptingLibraryModel libraryModel = jsLibraryManager.createLibrary(
          JstdLibraryUtil.LIBRARY_NAME,
          VfsUtilCore.toVirtualFileArray(libSourceFiles),
          VirtualFile.EMPTY_ARRAY,
          ArrayUtil.EMPTY_STRING_ARRAY,
          ScriptingLibraryModel.LibraryLevel.GLOBAL,
          false
        );
        JSLibraryMappings jsLibraryMappings = ServiceManager.getService(project, JSLibraryMappings.class);
        jsLibraryMappings.associate(null, libraryModel.getName());
        jsLibraryManager.commitChanges();
      }
    });
  }

  public void testResolveTestCaseFunction() throws Exception {
    String fileText = "Test<ref>Case('', {});";
    JSReferenceExpression ref = (JSReferenceExpression)configureByFileText(fileText, "sample.js");
    final PsiElement resolved = doResolve(ref);
    assertTrue(resolved instanceof JSFunction);
  }

  @Nullable
  public static PsiElement doResolve(@NotNull PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement element = unwrapResolveResult(resolveResult);
      if (element != null) {
        return element;
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement unwrapResolveResult(@NotNull ResolveResult resolveResult) {
    PsiElement resolvedElement = resolveResult.getElement();
    if (resolvedElement == null || !resolveResult.isValidResult()) {
      return null;
    }
    if (resolvedElement instanceof JSNamedElementProxy) {
      JSNamedElementProxy proxy = (JSNamedElementProxy) resolvedElement;
      return proxy.getElement();
    }
    return resolvedElement;
  }

}
