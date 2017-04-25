package com.intellij.flex.uiDesigner;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.TripleFunction;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IndexableFileSet;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DesignerTests {
  private static String testDataPath;
  private static VirtualFile testDataDir;

  public static VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot,
                                               VirtualFile[] files,
                                               @Nullable VirtualFile[] auxiliaryFiles,
                                               Module module,
                                               @Nullable TripleFunction<ModifiableRootModel, VirtualFile, List<String>, Void> moduleInitializer)
    throws Exception {
    return WriteAction.compute(() -> {
      VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
      //noinspection ConstantConditions
      dummyRoot.refresh(false, false);
      final VirtualFile sourceDir = dummyRoot.createChildDirectory(DesignerTests.class, "s");
      assert sourceDir != null;

      final IndexableFileSet indexableFileSet = new IndexableFileSet() {
        @Override
        public boolean isInSet(@NotNull final VirtualFile file) {
          return file.getFileSystem() == sourceDir.getFileSystem();
        }

        @Override
        public void iterateIndexableFilesIn(@NotNull final VirtualFile file, @NotNull final ContentIterator iterator) {
          if (file.isDirectory()) {
            for (VirtualFile child : file.getChildren()) {
              iterateIndexableFilesIn(child, iterator);
            }
          }
          else {
            iterator.processFile(file);
          }
        }
      };
      FileBasedIndex.getInstance().registerIndexableSet(indexableFileSet, module.getProject());

      Disposer.register(module, new Disposable() {
        @Override
        public void dispose() {
          FileBasedIndex.getInstance().removeIndexableSet(indexableFileSet);
          ApplicationManager.getApplication().runWriteAction(() -> {
            try {
              sourceDir.delete(null);
            }
            catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
        }
      });

      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

      VirtualFile[] toFiles = copyFiles(files, sourceDir, rawProjectRoot);
      if (auxiliaryFiles != null) {
        copyFiles(auxiliaryFiles, sourceDir, rawProjectRoot);
      }

      rootModel.addContentEntry(sourceDir).addSourceFolder(sourceDir, false);
      final List<String> libs = new ArrayList<>();
      if (moduleInitializer != null) {
        moduleInitializer.fun(rootModel, sourceDir, libs);
      }
      rootModel.commit();

      for (String path : libs) {
        VirtualFile virtualFile = path.charAt(0) != '/' ? getFile("lib", path) : getFile(path);
        FlexTestUtils.addLibrary(module, path, virtualFile.getParent().getPath(), virtualFile.getName(), null, null);
      }
      return toFiles;
    });
  }

  private static VirtualFile[] copyFiles(final VirtualFile[] fromFiles, final VirtualFile toDir, VirtualFile rawProjectRoot)
    throws IOException {
    final VirtualFile[] toFiles = new VirtualFile[fromFiles.length];
    final boolean rootSpecified = rawProjectRoot != null;
    for (int i = 0, n = fromFiles.length; i < n; i++) {
      VirtualFile fromFile = fromFiles[i];
      VirtualFile toP = toDir;
      if (rootSpecified) {
        final List<String> fromParents = new ArrayList<>(4);
        VirtualFile fromP = fromFile.getParent();
        if (!Comparing.equal(fromP, rawProjectRoot)) {
          do {
            fromParents.add(fromP.getName());
          }
          while (!Comparing.equal((fromP = fromP.getParent()), rawProjectRoot));

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

  @NotNull
  public static VirtualFile getFile(String path) {
    VirtualFile file = path.charAt(0) == '/' ? LocalFileSystem.getInstance().findFileByPath(path) : getTestDataDir().findFileByRelativePath(path);
    Assert.assertNotNull(file);
    return file;
  }

  @NotNull
  public static VirtualFile getFile(String baseDirectoryName, String relativePath) {
    //noinspection ConstantConditions
    return getTestDataDir().findChild(baseDirectoryName).findFileByRelativePath(relativePath);
  }

  public static VirtualFile getTestDataDir() {
    if (testDataDir == null) {
      testDataDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(DebugPathManager.getFudHome() + "/idea-plugin/testData");
      assert testDataDir != null;
      testDataDir.refresh(false, true);
    }
    return testDataDir;
  }

  public static String getTestDataPath() {
    if (testDataPath == null) {
      testDataPath = new File(DebugPathManager.getFudHome(), "idea-plugin/testData").getPath();
    }
    return testDataPath;
  }
}
