package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.ModuleTestCase;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class FlashUIDesignerBaseTestCase extends ModuleTestCase {
  private static String testDataPath;
  private VirtualFile sourceDir;

  @Override
  protected boolean isRunInWriteAction() {
    return false;
  }

  public static String getTestDataPath() {
    if (testDataPath == null) {
      testDataPath = DebugPathManager.getFudHome() + "/idea-plugin/testData";
    }
    return testDataPath;
  }

  protected static String getFudHome() {
    return DebugPathManager.getFudHome();
  }

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(),
                             DebugPathManager.getIdeaHome() + "/plugins/JavaScriptLanguage/testData/flex_highlighting/MockGumboSdk", false);
  }

  protected static VirtualFile getVFile(String path) {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path.charAt(0) == '/' ? path : (getTestDataPath() + '/' + path));
    assertNotNull(vFile);
    return vFile;
  }

  protected VirtualFile configureByFile(final String filepath) throws Exception {
    return configureByFile(getVFile(filepath));
  }

  protected VirtualFile configureByFile(final VirtualFile vFile) throws Exception {
    return configureByFiles(null, new VirtualFile[]{vFile}, null)[0];
  }

  /**
   * standard impl in CodeInsightTestCase is not suitable for us — in case of not null rawProjectRoot (we need test file in package),
   * we don't need "FileUtil.copyDir(projectRoot, toDirIO);"
   * also, skip openEditorsAndActivateLast
   */
  protected VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot, VirtualFile[] files, @Nullable VirtualFile[] auxiliaryFiles) throws Exception {
    final AccessToken token = WriteAction.start();
    final VirtualFile[] toFiles;
    try {
      final VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
      //noinspection ConstantConditions
      dummyRoot.refresh(false, false);
      sourceDir = dummyRoot.createChildDirectory(this, "s");
      assert sourceDir != null;
      //System.out.print("\ntemp dir l: " + toDir.getChildren().length);

      final ModuleRootManager rootManager = ModuleRootManager.getInstance(myModule);
      final ModifiableRootModel rootModel = rootManager.getModifiableModel();

      toFiles = copyFiles(files, sourceDir, rawProjectRoot);
      if (auxiliaryFiles != null) {
        copyFiles(auxiliaryFiles, sourceDir, rawProjectRoot);
      }

      rootModel.addContentEntry(sourceDir).addSourceFolder(sourceDir, false);
      modifyModule(rootModel, sourceDir);
      rootModel.commit();
    }
    finally {
      token.finish();
    }
    return toFiles;
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      super.tearDown();
    }
    finally {
      if (sourceDir != null) {
        final AccessToken token = WriteAction.start();
        try {
          sourceDir.delete(null);
        }
        finally {
          token.finish();
        }
      }
    }
  }

  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir) {
  }

  private static VirtualFile[] copyFiles(final VirtualFile[] fromFiles, final VirtualFile toDir, VirtualFile rawProjectRoot) throws IOException {
    final VirtualFile[] toFiles = new VirtualFile[fromFiles.length];
    final boolean rootSpecified = rawProjectRoot != null;
    for (int i = 0, n = fromFiles.length; i < n; i++) {
      VirtualFile fromFile = fromFiles[i];
      VirtualFile toP = toDir;
      if (rootSpecified) {
        final List<String> fromParents = new ArrayList<String>(4);
        VirtualFile fromP = fromFile.getParent();
        if (fromP != rawProjectRoot) {
          do {
            fromParents.add(fromP.getName());
          }
          while ((fromP = fromP.getParent()) != rawProjectRoot);

          for (int j = fromParents.size() - 1; j >= 0; j--) {
            toP = toP.createChildDirectory(null, fromParents.get(j));
          }
        }
      }
      final VirtualFile toFile = toP.createChildData(null, fromFile.getName());
      toFile.setBinaryContent(fromFile.contentsToByteArray());
      toFiles[i] = toFile;
    }

    return toFiles;
  }
}