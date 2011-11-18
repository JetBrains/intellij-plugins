package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
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
    return configureByFiles(null, vFile)[0];
  }

  /**
   * standard impl in CodeInsightTestCase is not suitable for us — in case of not null rawProjectRoot (we need test file in package),
   * we don't need "FileUtil.copyDir(projectRoot, toDirIO);"
   * also, skip openEditorsAndActivateLast
   */
  protected VirtualFile[] configureByFiles(@Nullable final VirtualFile rawProjectRoot, final VirtualFile... vFiles) throws Exception {
    final VirtualFile toDir;
    final AccessToken token = WriteAction.start();
    final VirtualFile[] files = new VirtualFile[vFiles.length];
    try {

      final VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
      //noinspection ConstantConditions
      dummyRoot.refresh(false, false);
      toDir = dummyRoot.createChildDirectory(this, "s");
      assert toDir != null;
      //System.out.print("\ntemp dir l: " + toDir.getChildren().length);

      final ModuleRootManager rootManager = ModuleRootManager.getInstance(myModule);
      final ModifiableRootModel rootModel = rootManager.getModifiableModel();


      // auxiliary files should be copied first
      copyFiles(vFiles, toDir, files, rawProjectRoot);

      rootModel.addContentEntry(toDir).addSourceFolder(toDir, false);
      modifyModule(rootModel, toDir);
      rootModel.commit();

      Disposer.register(myModule, new Disposable() {
        @Override
        public void dispose() {
          try {
            toDir.delete(this);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }
    finally {
      token.finish();
    }

    return files;
  }

  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir) {
  }

  private static void copyFiles(final VirtualFile[] fromFiles,
                                final VirtualFile toDir,
                                final VirtualFile[] toFiles,
                                VirtualFile rawProjectRoot) throws IOException {
    final boolean rootSpecified = rawProjectRoot != null;
    int toFileIndex = 0;
    for (int i = fromFiles.length - 1; i >= 0; i--) {
      final VirtualFile fromFile = fromFiles[i];
      VirtualFile toP = toDir;
      if (rootSpecified) {
        final List<String> fromParents = new ArrayList<String>(4);
        VirtualFile fromP;
        while ((fromP = fromFile.getParent()) != rawProjectRoot) {
          fromParents.add(fromP.getName());
        }

        for (int j = fromParents.size() - 1; j >= 0; j--) {
          toP = toP.createChildDirectory(null, fromParents.get(i));
        }
      }
      final VirtualFile toFile = toP.createChildData(null, fromFile.getName());
      toFile.setBinaryContent(fromFile.contentsToByteArray());
      toFiles[toFileIndex++] = toFile;
    }
  }
}