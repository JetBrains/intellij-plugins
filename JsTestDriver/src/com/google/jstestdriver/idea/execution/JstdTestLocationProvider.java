package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.assertFramework.JsTestFileByTestNameIndex;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructure;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.qunit.DefaultQUnitModuleStructure;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructure;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.google.jstestdriver.idea.util.EscapeUtils;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.FileUrlProvider;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testIntegration.TestLocationProvider;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class JstdTestLocationProvider implements TestLocationProvider {

  private static final String PROTOCOL_ID__CONFIG_FILE = "config";
  private static final String PROTOCOL_ID__TEST_CASE = "testCase";
  private static final String PROTOCOL_ID__TEST = "test";
  private static final String PROTOCOL_ID__BROWSER_ERROR = "browserError";

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
    final Location location;
    if (PROTOCOL_ID__CONFIG_FILE.equals(protocolId)) {
      location = findConfigFile(locationData, project);
    }
    else if (PROTOCOL_ID__TEST_CASE.equals(protocolId) || PROTOCOL_ID__TEST.equals(protocolId)) {
      location = findTest(locationData, project);
    }
    else if (PROTOCOL_ID__BROWSER_ERROR.equals(protocolId)) {
      location = findJsFile(locationData, project);
    }
    else {
      location = null;
    }
    if (location != null) {
      return Collections.singletonList(location);
    }
    return Collections.emptyList();
  }

  @Nullable
  private static Location findJsFile(@NotNull String locationData, Project project) {
    List<String> components = EscapeUtils.split(locationData, ':');
    if (components.size() != 3) {
      return null;
    }
    String jsFilePath = components.get(0);
    Integer lineNumber = toInteger(components.get(1));
    int line = lineNumber != null ? lineNumber : 1;

    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(jsFilePath));
    if (virtualFile != null && virtualFile.isValid()) {
      return FileUrlProvider.createLocationFor(project, virtualFile, line);
    }
    return null;
  }

  private static Integer toInteger(@NotNull String s) {
    try {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Nullable
  private static Location<PsiElement> findTest(final @NotNull String locationData, Project project) {
    List<String> path = EscapeUtils.split(locationData, ':');
    if (path.size() < 2) {
      return null;
    }
    String jsTestFilePath = path.get(0);
    String testCaseName = path.get(1);
    String testMethodName = path.size() > 2 ? path.get(2) : null;

    File jsTestFile = new File(jsTestFilePath);

    final PsiElement psiElement;
    if (jsTestFile.isFile() && jsTestFile.isAbsolute()) {
      VirtualFile jsTestVirtualFile = VfsUtil.findFileByIoFile(jsTestFile, false);
      if (jsTestVirtualFile == null || !jsTestVirtualFile.isValid()) {
        return null;
      }
      String name = jsTestVirtualFile.getName().toLowerCase();
      if (testMethodName != null && testCaseName.equals(DefaultQUnitModuleStructure.NAME)
          && StringUtil.startsWithIgnoreCase(name, "QUnitAdapter")) {
        psiElement = findTestFromQUnitDefaultModule(project, testMethodName);
      }
      else {
        psiElement = NavUtils.findPsiLocation(
          project,
          jsTestVirtualFile,
          testCaseName,
          testMethodName
        );
      }
    }
    else {
      psiElement = findJasmineTestLocation(project, testCaseName, testMethodName);
    }
    if (psiElement != null && psiElement.isValid()) {
      return PsiLocation.fromPsiElement(psiElement);
    }
    return null;
  }

  @Nullable
  private static PsiElement findTestFromQUnitDefaultModule(@NotNull Project project,
                                                           @NotNull String testMethodName) {
    GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    String key = JsTestFileByTestNameIndex.getQUnitTestNameKey(testMethodName);
    List<VirtualFile> jsTestVirtualFiles = JsTestFileByTestNameIndex.findJsTestFilesByNameInScope(key, scope);
    List<VirtualFile> validJsTestVirtualFiles = filterVirtualFiles(jsTestVirtualFiles);

    for (VirtualFile jsTestVirtualFile : validJsTestVirtualFiles) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(jsTestVirtualFile);
      if (psiFile instanceof JSFile) {
        JSFile jsFile = (JSFile) psiFile;
        QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
        QUnitFileStructure qunitFileStructure = builder.fetchCachedTestFileStructure(jsFile);
        PsiElement element = qunitFileStructure.findPsiElement(DefaultQUnitModuleStructure.NAME, testMethodName);
        if (element != null && element.isValid()) {
          return element;
        }
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement findJasmineTestLocation(@NotNull Project project,
                                                    @NotNull String testCaseName,
                                                    @Nullable String testMethodName) {
    GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    List<VirtualFile> jsTestVirtualFiles = JsTestFileByTestNameIndex.findJsTestFilesByNameInScope(testCaseName, scope);
    List<VirtualFile> validJsTestVirtualFiles = filterVirtualFiles(jsTestVirtualFiles);

    for (VirtualFile jsTestVirtualFile : validJsTestVirtualFiles) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(jsTestVirtualFile);
      if (psiFile instanceof JSFile) {
        JSFile jsFile = (JSFile) psiFile;
        JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
        JasmineFileStructure jasmineFileStructure = builder.fetchCachedTestFileStructure(jsFile);
        PsiElement element = jasmineFileStructure.findPsiElement(testCaseName, testMethodName);
        if (element != null && element.isValid()) {
          return element;
        }
      }
    }
    return null;
  }

  private static List<VirtualFile> filterVirtualFiles(@NotNull List<VirtualFile> files) {
    boolean ok = true;
    for (VirtualFile file : files) {
      if (!file.isValid()) {
        ok = false;
        break;
      }
    }
    if (ok) {
      return files;
    }
    return ContainerUtil.filter(files, new Condition<VirtualFile>() {
      @Override
      public boolean value(VirtualFile file) {
        return file.isValid();
      }
    });
  }

  @Nullable
  private static Location findConfigFile(@NotNull String locationData, Project project) {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(locationData));
    if (virtualFile != null && virtualFile.isValid()) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      if (psiFile != null && psiFile.isValid()) {
        return PsiLocation.fromPsiElement(psiFile);
      }
    }
    return null;
  }
}
