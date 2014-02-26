package com.intellij.javascript.karma.execution;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.javascript.testFramework.JsTestFileByTestNameIndex;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure;
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder;
import com.intellij.javascript.testFramework.qunit.DefaultQUnitModuleStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class KarmaTestLocationProvider implements TestLocationProvider {

  private static final String PROTOCOL_ID__CONFIG_FILE = "config";
  private static final String PROTOCOL_ID__TEST_SUITE = "suite";
  private static final String PROTOCOL_ID__TEST = "test";

  private final Project myProject;

  public KarmaTestLocationProvider(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
    final Location location;
    if (PROTOCOL_ID__CONFIG_FILE.equals(protocolId)) {
      location = getConfigLocation(locationData);
    }
    else if (PROTOCOL_ID__TEST_SUITE.equals(protocolId)) {
      location = getTestLocation(locationData, true);
    }
    else if (PROTOCOL_ID__TEST.equals(protocolId)) {
      location = getTestLocation(locationData, false);
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
  private Location<PsiFile> getConfigLocation(@NotNull String locationData) {    VirtualFile
    virtualFile = VfsUtil.findFileByIoFile(new File(locationData), false);
    if (virtualFile != null && virtualFile.isValid()) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
      if (psiFile != null && psiFile.isValid()) {
        return PsiLocation.fromPsiElement(psiFile);
      }
    }
    return null;
  }

  @Nullable
  private Location getTestLocation(@NotNull String locationData, boolean isSuite) {
    List<String> path = EscapeUtils.split(locationData, '.');
    if (path.isEmpty()) {
      return null;
    }
    final List<String> suiteNames;
    final String testName;
    if (isSuite) {
      suiteNames = path;
      testName = null;
    }
    else {
      suiteNames = path.subList(0, path.size() - 1);
      testName = path.get(path.size() - 1);
    }
    PsiElement psiElement = findJasmineElement(suiteNames, testName);
    if (psiElement == null) {
      String moduleName = null;
      if (suiteNames.size() == 0) {
        moduleName = DefaultQUnitModuleStructure.NAME;
      }
      else if (suiteNames.size() == 1) {
        moduleName = suiteNames.get(0);
      }
      if (moduleName != null) {
        psiElement = findQUnitElement(moduleName, testName);
      }
    }
    if (psiElement != null) {
      return PsiLocation.fromPsiElement(psiElement);
    }
    return null;
  }

  @Nullable
  private PsiElement findJasmineElement(@NotNull List<String> suiteNames, @Nullable String testName) {
    String suiteKey = JsTestFileByTestNameIndex.createJasmineKey(suiteNames);
    GlobalSearchScope scope = GlobalSearchScope.projectScope(myProject);
    List<VirtualFile> jsTestVirtualFiles = JsTestFileByTestNameIndex.findJsTestFilesByNameInScope(myProject, suiteKey, scope);
    for (VirtualFile file : jsTestVirtualFiles) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
      if (psiFile instanceof JSFile) {
        JSFile jsFile = (JSFile) psiFile;
        JasmineFileStructureBuilder builder = JasmineFileStructureBuilder.getInstance();
        JasmineFileStructure jasmineFileStructure = builder.fetchCachedTestFileStructure(jsFile);
        PsiElement element = jasmineFileStructure.findPsiElement(suiteNames, testName);
        if (element != null && element.isValid()) {
          return element;
        }
      }
    }
    return null;
  }

  @Nullable
  private PsiElement findQUnitElement(@NotNull String moduleName, @Nullable String testName) {
    String qunitKey = JsTestFileByTestNameIndex.createQUnitKey(moduleName, testName);
    GlobalSearchScope scope = GlobalSearchScope.projectScope(myProject);
    List<VirtualFile> jsTestVirtualFiles = JsTestFileByTestNameIndex.findJsTestFilesByNameInScope(myProject, qunitKey, scope);
    for (VirtualFile file : jsTestVirtualFiles) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
      if (psiFile instanceof JSFile) {
        JSFile jsFile = (JSFile) psiFile;
        QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
        QUnitFileStructure qunitFileStructure = builder.fetchCachedTestFileStructure(jsFile);
        PsiElement element = qunitFileStructure.findPsiElement(moduleName, testName);
        if (element != null && element.isValid()) {
          return element;
        }
      }
    }
    return null;
  }

}
