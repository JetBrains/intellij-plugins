package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.assertFramework.support.AbstractMethodBasedInspection;
import com.google.jstestdriver.idea.assertFramework.support.JsLibraryHelper;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class JstdAssertionFrameworkSupportInspection extends AbstractMethodBasedInspection {

  private static final Logger LOG = Logger.getInstance(JstdAssertionFrameworkSupportInspection.class);
  private static final AddJstdLibraryLocalQuickFix ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX = new AddJstdLibraryLocalQuickFix();

  @Override
  protected boolean isSuitableMethod(String methodName, JSExpression[] methodArguments) {
    if (methodArguments.length < 1) {
      return false;
    }
    if (!JsPsiUtils.isStringElement(methodArguments[0])) {
      return false;
    }
    if ("TestCase".equals(methodName) || "AsyncTestCase".equals(methodName)) {
      if (methodArguments.length == 1) {
        return true;
      }
      if (methodArguments.length == 2 && JsPsiUtils.isObjectElement(methodArguments[1])) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected LocalQuickFix getQuickFix() {
    return ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX;
  }

  private static class AddJstdLibraryLocalQuickFix implements LocalQuickFix {

    @NotNull
    @Override
    public String getName() {
      return "Add JsTestDriver assertion framework support";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      installLibrary(project);
      PsiElement psiElement = descriptor.getPsiElement();
      PsiFile psiFile = psiElement.getContainingFile();
      VirtualFile virtualFile = psiFile.getVirtualFile();

//      System.out.println(Thread.currentThread());
//      PsiModificationTracker tracker = ServiceManager.getService(project, PsiModificationTracker.class);
//      if (tracker instanceof PsiModificationTrackerImpl) {
//        PsiModificationTrackerImpl trackerImpl = (PsiModificationTrackerImpl) tracker;
//        trackerImpl.incCounter();
//        System.out.println(PsiModificationTrackerImpl.class.getName() + "#incCounter() called");
//      }
//
//      PsiManager psiManager = PsiManager.getInstance(project);
//      if (psiManager instanceof PsiManagerImpl) {
//        PsiManagerImpl psiManagerImpl = (PsiManagerImpl) psiManager;
//        psiManagerImpl.dropResolveCaches();
//        System.out.println(PsiManagerImpl.class.getName() + "#dropResolveCaches() called");
//      }
//
//      ProjectRootManagerEx projectRootManagerEx = ProjectRootManagerEx.getInstanceEx(project);
//      projectRootManagerEx.makeRootsChange(EmptyRunnable.getInstance(), false, true);
//
//      System.out.println(ProjectRootManagerEx.class.getName() + "#makeRootsChange() called");
//
//      ResolveCache.getInstance(project).clearCache(true);
//      ResolveCache.getInstance(project).clearCache(false);
//      JSResolveUtil.clearResolveCaches(psiFile);
//
//      DaemonCodeAnalyzer analyzer = DaemonCodeAnalyzer.getInstance(project);
//      analyzer.restart();
//      System.out.println(DaemonCodeAnalyzer.class.getName() + "#restart() called");

      FileContentUtil.reparseFiles(project, Arrays.asList(virtualFile), true);
    }

    private static void installLibrary(@NotNull Project project) {
      List<VirtualFile> sources = VfsUtils.findVirtualFilesByResourceNames(
        JstdDefaultAssertionFrameworkSrcMarker.class,
        new String[]{"Asserts.js", "TestCase.js"}
      );
      JsLibraryHelper libraryHelper = new JsLibraryHelper(project);
      String libraryName = "JsTestDriver Assertion Framework";
      ScriptingLibraryModel libraryModel = libraryHelper.createJsLibrary(libraryName, sources);
      String dialogTitle = "Adding JsTestDriver assertion framework support";
      if (libraryModel == null) {
        Messages.showErrorDialog("Unable to create '" + libraryName + "' JavaScript library", dialogTitle);
        return;
      }
      VirtualFile projectRootDir = project.getBaseDir();
      if (projectRootDir == null) {
        LOG.error("Project baseDir is null!");
        return;
      }
      boolean associated = libraryHelper.associateLibraryWithDir(libraryModel, projectRootDir);
      if (!associated) {
        Messages.showErrorDialog("Unable to associate '" + libraryName + "' JavaScript library with project", dialogTitle);
      }
    }
  }

}
