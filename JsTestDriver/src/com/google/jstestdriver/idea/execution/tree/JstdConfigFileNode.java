package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.TestResult;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class JstdConfigFileNode extends Node {

  private final Map<String, TestCaseNode> myTestCaseMap = Maps.newHashMap();
  private TestResult.Result worstResult = TestResult.Result.passed;
  private final String myAbsoluteFilePath;
  private final VirtualFile myVirtualFile;

  public JstdConfigFileNode(VirtualFile directory, @NotNull String absoluteFilePath) {
    myVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(absoluteFilePath));
    String displayPath = calcDisplayPath(directory, myVirtualFile, absoluteFilePath);
    setTestProxy(new SMTestProxyWithPrinterAndLocation(displayPath, true, new LocationProvider() {
      @Override
      public Location provideLocation(Project project) {
        if (myVirtualFile == null) {
          return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(myVirtualFile);
        if (psiFile == null) {
          return null;
        }
        return PsiLocation.fromPsiElement(psiFile);
      }
    }));
    this.myAbsoluteFilePath = absoluteFilePath;
  }

  private static String calcDisplayPath(VirtualFile directory, VirtualFile virtualFile, String absoluteFilePath) {
    if (directory == null || virtualFile == null) return absoluteFilePath;
    String directoryPath = directory.getPath();
    String vfp = virtualFile.getPath();
    if (vfp.startsWith(directoryPath)) {
      String res = vfp.substring(directoryPath.length());
      return res.startsWith("/") || res.startsWith("\\") ? res.substring(1) : res;
    }
    return absoluteFilePath;
  }

  @Nullable
  public VirtualFile getVirtualFile() {
    return myVirtualFile;
  }

  @NotNull
  public String getAbsoluteFilePath() {
    return myAbsoluteFilePath;
  }

  public boolean allTestCasesComplete() {
    for (TestCaseNode testCaseNode : myTestCaseMap.values()) {
      if (!testCaseNode.allTestsComplete()) {
        return false;
      }
    }
    return true;
  }

  public void setTestFailed(TestResult.Result result) {
    if (result == TestResult.Result.error && worstResult != TestResult.Result.error) {
      getTestProxy().setTestFailed("", "", true);
    } else if (result == TestResult.Result.failed && worstResult == TestResult.Result.passed) {
      getTestProxy().setTestFailed("", "", false);
    }
  }

  public TestCaseNode getTestCaseNode(String testCase) {
    return myTestCaseMap.get(testCase);
  }

  public void registerTestCaseNode(@NotNull TestCaseNode testCaseNode) {
    myTestCaseMap.put(testCaseNode.getName(), testCaseNode);
  }

}
