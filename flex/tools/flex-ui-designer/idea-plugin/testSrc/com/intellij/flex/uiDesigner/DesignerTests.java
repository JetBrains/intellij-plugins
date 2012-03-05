package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.TripleFunction;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IndexableFileSet;
import junit.framework.Assert;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DesignerTests {
  private static String testDataPath;

  public static VirtualFile[] configureByFiles(@Nullable VirtualFile rawProjectRoot,
                                               VirtualFile[] files,
                                               @Nullable VirtualFile[] auxiliaryFiles,
                                               Module module,
                                               @Nullable TripleFunction<ModifiableRootModel, VirtualFile, List<String>, Void> moduleInitializer)
    throws Exception {
    AccessToken token = WriteAction.start();
    VirtualFile[] toFiles;
    try {
      VirtualFile dummyRoot = VirtualFileManager.getInstance().findFileByUrl("temp:///");
      //noinspection ConstantConditions
      dummyRoot.refresh(false, false);
      final VirtualFile sourceDir = dummyRoot.createChildDirectory(DesignerTests.class, "s");
      assert sourceDir != null;

      final IndexableFileSet indexableFileSet = new IndexableFileSet() {
        @Override
        public boolean isInSet(final VirtualFile file) {
          return file.getFileSystem() == sourceDir.getFileSystem();
        }

        @Override
        public void iterateIndexableFilesIn(final VirtualFile file, final ContentIterator iterator) {
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
          final AccessToken token = WriteAction.start();
          try {
            sourceDir.delete(DesignerTests.class);
          }
          catch (IOException e) {
            throw new AssertionError(e);
          }
          finally {
            token.finish();

            FileBasedIndex.getInstance().removeIndexableSet(indexableFileSet);
          }
        }
      });

      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

      toFiles = copyFiles(files, sourceDir, rawProjectRoot);
      if (auxiliaryFiles != null) {
        copyFiles(auxiliaryFiles, sourceDir, rawProjectRoot);
      }

      rootModel.addContentEntry(sourceDir).addSourceFolder(sourceDir, false);
      final List<String> libs = new ArrayList<String>();
      if (moduleInitializer != null) {
        moduleInitializer.fun(rootModel, sourceDir, libs);
      }
      rootModel.commit();

      for (String path : libs) {
        if (path.charAt(0) != '/') {
          path = getTestDataPath() + "/lib/" + path;
        }

        VirtualFile virtualFile = getVFile(path);
        JSTestUtils.addLibrary(module, path, virtualFile.getParent().getPath(), virtualFile.getName(), null, null);
      }
    }
    finally {
      token.finish();
    }

    return toFiles;
  }

  private static VirtualFile[] copyFiles(final VirtualFile[] fromFiles, final VirtualFile toDir, VirtualFile rawProjectRoot)
    throws IOException {
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

  public static VirtualFile getVFile(String path) {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(path.charAt(0) == '/' ? path : (getTestDataPath() + '/' + path));
    Assert.assertNotNull(vFile);
    return vFile;
  }

  public static String getTestDataPath() {
    if (testDataPath == null) {
      testDataPath = DebugPathManager.getFudHome() + "/idea-plugin/testData";
    }
    return testDataPath;
  }
}
