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
import java.util.Collection;
import java.util.Map;

class JstdConfigFileNode extends Node {

  private final BrowserNode myBrowserNode;
  private final VirtualFile myVirtualFile;
  private final String myAbsoluteFilePath;
  private final Map<String, TestCaseNode> myTestCaseMap = Maps.newHashMap();
  private boolean myFake;

  public JstdConfigFileNode(@NotNull BrowserNode browserNode, @Nullable VirtualFile directory, @NotNull String absoluteFilePath, boolean fake) {
    super(createTestProxy(directory, absoluteFilePath));
    myBrowserNode = browserNode;
    myVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(absoluteFilePath));
    myAbsoluteFilePath = absoluteFilePath;
    myFake = fake;
    browserNode.registerJstdConfigFileNode(this);
  }

  public static SMTestProxyWithPrinterAndLocation createTestProxy(@Nullable VirtualFile directory, @NotNull String absoluteFilePath) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(absoluteFilePath));
    String displayPath = calcDisplayPath(directory, virtualFile, absoluteFilePath);
    return new SMTestProxyWithPrinterAndLocation(displayPath, true, new LocationProvider() {
      @Override
      public Location provideLocation(@NotNull Project project) {
        if (virtualFile == null) {
          return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        return PsiLocation.fromPsiElement(psiFile);
      }
    });
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

  public BrowserNode getBrowserNode() {
    return myBrowserNode;
  }

  @Nullable
  public VirtualFile getVirtualFile() {
    return myVirtualFile;
  }

  @NotNull
  public String getAbsoluteFilePath() {
    return myAbsoluteFilePath;
  }

  @NotNull
  public File getConfigFile() {
    return new File(myAbsoluteFilePath);
  }

  public TestCaseNode getTestCaseNode(String testCase) {
    return myTestCaseMap.get(testCase);
  }

  public void registerTestCaseNode(@NotNull TestCaseNode testCaseNode) {
    myTestCaseMap.put(testCaseNode.getName(), testCaseNode);
  }

  @Override
  public Collection<? extends Node> getChildren() {
    return myTestCaseMap.values();
  }

  @Override
  public void setTestFailed(TestResult.Result result) {
    if (!myFake) {
      super.setTestFailed(result);
    }
  }
}
