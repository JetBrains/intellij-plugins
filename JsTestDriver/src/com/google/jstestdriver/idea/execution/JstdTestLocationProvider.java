package com.google.jstestdriver.idea.execution;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.JstdTestMethodNameRefiner;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.FileUrlProvider;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.javascript.testFramework.JsTestFileByTestNameIndex;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.javascript.testFramework.jasmine.JasmineSpecStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineSuiteStructure;
import com.intellij.javascript.testFramework.qunit.DefaultQUnitModuleStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* @author Sergey Simonchik
*/
public class JstdTestLocationProvider implements SMTestLocator {
  private static final String PROTOCOL_ID__CONFIG_FILE = "config";
  private static final String PROTOCOL_ID__TEST_CASE = "testCase";
  private static final String PROTOCOL_ID__TEST = "test";
  private static final String PROTOCOL_ID__BROWSER_ERROR = "browserError";

  public static final JstdTestLocationProvider INSTANCE = new JstdTestLocationProvider();

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
    final Location location;
    if (PROTOCOL_ID__CONFIG_FILE.equals(protocol)) {
      location = findConfigFile(path, project);
    }
    else if (PROTOCOL_ID__TEST_CASE.equals(protocol) || PROTOCOL_ID__TEST.equals(protocol)) {
      location = findTest(path, project);
    }
    else if (PROTOCOL_ID__BROWSER_ERROR.equals(protocol)) {
      location = findJsFile(path, project);
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
      if (testMethodName != null
          && testCaseName.equals(DefaultQUnitModuleStructure.NAME)
          && StringUtil.startsWithIgnoreCase(jsTestVirtualFile.getName(), "QUnitAdapter")) {
        psiElement = findTestFromQUnitDefaultModule(project, testMethodName);
      }
      else {
        psiElement = NavUtils.findPsiLocation(
          project,
          jsTestVirtualFile,
          testCaseName,
          testMethodName,
          JstdTestMethodNameRefiner.INSTANCE
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
    testMethodName = StringUtil.trimStart(testMethodName, "test ");
    String key = JsTestFileByTestNameIndex.createQUnitKeyForTestFromDefaultModule(testMethodName);
    List<VirtualFile> jsTestVirtualFiles = JsTestFileByTestNameIndex.findFilesByKey(key, scope);
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
    VirtualFile file = findJasmineTestFileSource(project, testCaseName);
    if (file == null) {
      return null;
    }
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (psiFile instanceof JSFile) {
      JSFile jsFile = (JSFile) psiFile;
      JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
      JasmineFileStructure jasmineFileStructure = builder.fetchCachedTestFileStructure(jsFile);
      Map<String, JasmineSuiteStructure> map = Maps.newHashMap();
      for (JasmineSuiteStructure suite : jasmineFileStructure.getSuites()) {
        visitSuite("", map, suite);
      }
      JasmineSuiteStructure suite = map.get(testCaseName);
      if (suite != null) {
        if (testMethodName == null) {
          return suite.getEnclosingCallExpression();
        }
        JasmineSpecStructure spec = suite.getInnerSpecByName(testMethodName);
        if (spec != null) {
          return spec.getEnclosingCallExpression();
        }
      }
    }
    return null;
  }

  private static void visitSuite(@NotNull String prefix,
                                 @NotNull Map<String, JasmineSuiteStructure> map,
                                 @NotNull JasmineSuiteStructure suite) {
    final String joinedSuitesName;
    if (prefix.isEmpty()) {
      joinedSuitesName = suite.getName();
    }
    else {
      joinedSuitesName = prefix + " " + suite.getName();
    }
    map.put(joinedSuitesName, suite);
    for (JasmineSuiteStructure child : suite.getSuites()) {
      visitSuite(joinedSuitesName, map, child);
    }
  }

  @Nullable
  private static VirtualFile findJasmineTestFileSource(@NotNull Project project, @NotNull String joinedSuites) {
    GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
    joinedSuites += ' ';
    int lastSpaceInd = joinedSuites.indexOf(' ');
    while (lastSpaceInd >= 0) {
      String topLevelSuiteName = joinedSuites.substring(0, lastSpaceInd);
      String key = JsTestFileByTestNameIndex.createJasmineKey(Collections.singletonList(topLevelSuiteName));
      List<VirtualFile> files = JsTestFileByTestNameIndex.findFilesByKey(key, scope);
      for (VirtualFile file : files) {
        if (file.isValid()) {
          return file;
        }
      }
      lastSpaceInd = joinedSuites.indexOf(' ', lastSpaceInd + 1);
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
    return ContainerUtil.filter(files, file -> file.isValid());
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
