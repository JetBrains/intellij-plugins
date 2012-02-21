package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerBundle;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions.ResourceFilesMode;

public class FlexResourceCompiler {

  private final CompileContext myContext;
  private final Map<Module, Collection<FlexIdeBuildConfiguration>> myModuleToBCs;
  private final ProjectFileIndex myFileIndex;
  private final CompilerConfiguration myCompilerConfiguration;

  /**
   * @param moduleToBCs all build configurations in this map must have 'skip compilation' disabled and resource files processing mode not equal to ResourceFilesMode.None.
   */
  public FlexResourceCompiler(final CompileContext context, final Map<Module, Collection<FlexIdeBuildConfiguration>> moduleToBCs) {
    myContext = context;
    myModuleToBCs = moduleToBCs;
    myFileIndex = ProjectRootManager.getInstance(myContext.getProject()).getFileIndex();
    myCompilerConfiguration = CompilerConfiguration.getInstance(myContext.getProject());
  }

  public void processResourceFiles() {
    final Collection<Pair<String, String>> sourceAndTargetFilePaths =
      ApplicationManager.getApplication().runReadAction(new Computable<Collection<Pair<String, String>>>() {
        public Collection<Pair<String, String>> compute() {
          final Collection<Pair<String, String>> paths = new ArrayList<Pair<String, String>>();

          for (Map.Entry<Module, Collection<FlexIdeBuildConfiguration>> entry : myModuleToBCs.entrySet()) {
            final Module module = entry.getKey();
            final Collection<FlexIdeBuildConfiguration> bcs = entry.getValue();
            if (bcs.isEmpty()) continue;

            appendPathsForModule(paths, module, bcs);
          }

          return paths;
        }
      });

    doCopy(sourceAndTargetFilePaths);
  }

  private void appendPathsForModule(final Collection<Pair<String, String>> sourceAndTargetFilePaths,
                                    final Module module,
                                    final Collection<FlexIdeBuildConfiguration> bcs) {
    for (final VirtualFile srcRoot : ModuleRootManager.getInstance(module).getSourceRoots()) {
      final boolean isTestRoot = myFileIndex.isInTestSourceContent(srcRoot);

      myFileIndex.iterateContentUnderDirectory(srcRoot, new ContentIterator() {
        public boolean processFile(final VirtualFile file) {
          if (file.isDirectory() || myCompilerConfiguration.isExcludedFromCompilation(file)) return true;

          final String relativePath = VfsUtilCore.getRelativePath(file, srcRoot, '/');

          for (FlexIdeBuildConfiguration bc : bcs) {
            assert !bc.isSkipCompile() && BCUtils.canHaveResourceFiles(bc.getNature()) &&
                   bc.getCompilerOptions().getResourceFilesMode() != ResourceFilesMode.None : bc.getName();

            if (isTestRoot) {
              if (BCUtils.isFlexUnitBC(module, bc) && !isSourceFile(file)) {
                final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
                final String outputUrl = compilerModuleExtension == null ? null : compilerModuleExtension.getCompilerOutputUrlForTests();
                if (outputUrl != null) {
                  sourceAndTargetFilePaths.add(Pair.create(file.getPath(), VfsUtil.urlToPath(outputUrl) + "/" + relativePath));
                }
              }
            }
            else {
              final ResourceFilesMode mode = bc.getCompilerOptions().getResourceFilesMode();
              if (mode == ResourceFilesMode.All && !isSourceFile(file) ||
                  mode == ResourceFilesMode.ResourcePatterns && myCompilerConfiguration.isResourceFile(file)) {
                sourceAndTargetFilePaths.add(Pair.create(file.getPath(), bc.getOutputFolder() + "/" + relativePath));
              }
            }
          }

          return true;
        }
      });
    }
  }

  private void doCopy(final Collection<Pair<String, String>> sourceAndTargetFilePaths) {
    final Collection<File> filesToRefresh = new THashSet<File>();

    for (Pair<String, String> sourceAndTargetFilePath : sourceAndTargetFilePaths) {
      final File sourceFile = new File(sourceAndTargetFilePath.first);
      final File targetFile = new File(sourceAndTargetFilePath.second);
      try {
        FileUtil.copyContent(sourceFile, targetFile);
        filesToRefresh.add(targetFile);
      }
      catch (IOException e) {
        myContext.addMessage(CompilerMessageCategory.ERROR,
                             CompilerBundle.message("error.copying", sourceFile.getPath(), targetFile.getPath(), e.getMessage()),
                             VfsUtil.pathToUrl(sourceFile.getPath()), -1, -1);
      }
    }

    CompilerUtil.refreshIOFiles(filesToRefresh);
  }

  private static boolean isSourceFile(final VirtualFile file) {
    final String ext = file.getExtension();
    return ext != null && (ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg"));
  }
}

