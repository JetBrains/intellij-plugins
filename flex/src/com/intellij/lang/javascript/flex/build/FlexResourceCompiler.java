package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.compiler.ex.CompilerPathsEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FlexResourceCompiler implements SourceProcessingCompiler {

  private static final Logger LOG = Logger.getInstance(FlexResourceCompiler.class.getName());

  @NotNull
  public String getDescription() {
    return "Flex Resource Compiler";
  }

  public boolean validateConfiguration(final CompileScope scope) {
    if (CompilerPathsEx.CLEAR_ALL_OUTPUTS_KEY.get(scope) == Boolean.TRUE) {
      try {
        final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCs = FlexCompiler.getModulesAndBCsToCompile(scope);
        Set<VirtualFile> outputs = new HashSet<VirtualFile>();
        for (Pair<Module, FlexIdeBuildConfiguration> pair : modulesAndBCs) {
          String outputFilePath = pair.second.getActualOutputFilePath();
          VirtualFile outputFolder = LocalFileSystem.getInstance().findFileByPath(PathUtil.getParentPath(outputFilePath));
          ContainerUtil.addIfNotNull(outputs, outputFolder);
        }

        Project project = scope.getAffectedModules()[0].getProject();
        Set<VirtualFile> affectedOutputPaths = new HashSet<VirtualFile>();
        CompilerUtil.computeIntersectingPaths(project, outputs, affectedOutputPaths);
        if (!affectedOutputPaths.isEmpty()) {
          if (CompilerUtil.askUserToContinueWithNoClearing(project, affectedOutputPaths)) {
            CompilerPathsEx.CLEAR_ALL_OUTPUTS_KEY.set(scope, false);
            return true;
          }
          else {
            return false;
          }
        }
      }
      catch (ConfigurationException ignored) {
        // FlexCompiler.validateConfiguration() will report in its turn
      }
    }
    return true; // will be validated in FlexCompiler.validateConfiguration()
  }

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    ProcessingItem[] items = ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>() {
      public ProcessingItem[] compute() {
        try {
          final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCs =
            FlexCompiler.getModulesAndBCsToCompile(context.getCompileScope());

          final Collection<FlexResourceProcessingItem> result = new ArrayList<FlexResourceProcessingItem>();
          final Collection<Module> modules = new THashSet<Module>();

          for (Pair<Module, FlexIdeBuildConfiguration> bc : modulesAndBCs) {
            final Module module = bc.first;
            if (modules.add(module)) {
              appendItemsForModule(result, module);
            }
          }

          return result.toArray(new ProcessingItem[result.size()]);
        }
        catch (ConfigurationException e) {
          // the error will be reported by FlexCompiler.validateConfiguration()
          return ProcessingItem.EMPTY_ARRAY;
        }
      }
    });

    if (items.length == 0 && CompilerPathsEx.CLEAR_ALL_OUTPUTS_KEY.get(context.getCompileScope()) == Boolean.TRUE) {
      return new ProcessingItem[]{new FakeProcessingItem(context.getProject().getBaseDir())};
    }
    return items;
  }

  private static void appendItemsForModule(final Collection<FlexResourceProcessingItem> processingItems, final Module module) {
    final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    final ModuleFileIndex fileIndex = rootManager.getFileIndex();
    final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(module.getProject());

    for (final VirtualFile srcRoot : rootManager.getSourceRoots()) {
      final boolean isTestRoot = fileIndex.isInTestSourceContent(srcRoot);

      fileIndex.iterateContentUnderDirectory(srcRoot, new ContentIterator() {
        public boolean processFile(final VirtualFile file) {
          if (file.isDirectory() || compilerConfiguration.isExcludedFromCompilation(file)) return true;

          final String relativePath = VfsUtilCore.getRelativePath(file, srcRoot, '/');
          final Set<String> targetPaths = new THashSet<String>();

          if (isTestRoot) {
            if (!FlexCommonUtils.isSourceFile(file.getName())) {
              final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
              final String outputUrl = compilerModuleExtension == null ? null : compilerModuleExtension.getCompilerOutputUrlForTests();
              if (outputUrl != null) {
                targetPaths.add(VfsUtilCore.urlToPath(outputUrl) + "/" + relativePath);
              }
            }
          }
          else {
            for (FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
              if (bc.isSkipCompile() || !BCUtils.canHaveResourceFiles(bc.getNature()) ||
                  bc.getCompilerOptions().getResourceFilesMode() == CompilerOptions.ResourceFilesMode.None) {
                continue;
              }

              final CompilerOptions.ResourceFilesMode mode = bc.getCompilerOptions().getResourceFilesMode();
              if (mode == CompilerOptions.ResourceFilesMode.All && !FlexCommonUtils.isSourceFile(file.getName()) ||
                  mode == CompilerOptions.ResourceFilesMode.ResourcePatterns && compilerConfiguration.isResourceFile(file)) {
                final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());
                targetPaths.add(outputFolder + "/" + relativePath);
              }
            }
          }

          if (targetPaths.size() > 0) {
            processingItems.add(new FlexResourceProcessingItem(file, targetPaths));
          }

          return true;
        }
      });
    }
  }

  public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items) {
    if (CompilerPathsEx.CLEAR_ALL_OUTPUTS_KEY.get(context.getCompileScope()) == Boolean.TRUE) {
      context.getProgressIndicator().pushState();
      context.getProgressIndicator().setText(CompilerBundle.message("progress.clearing.output"));
      Collection<File> outputDirectories = new HashSet<File>();
      try {
        Collection<Pair<Module, FlexIdeBuildConfiguration>> toCompile = FlexCompiler.getModulesAndBCsToCompile(context.getCompileScope());
        for (Pair<Module, FlexIdeBuildConfiguration> pair : toCompile) {
          outputDirectories.add(new File(PathUtil.getParentPath(pair.second.getActualOutputFilePath())));
        }
      }
      catch (ConfigurationException e) {
        LOG.error("Validation error unexpected", e);
      }
      CompilerUtil.clearOutputDirectories(outputDirectories);
      context.getProgressIndicator().popState();
    }

    context.getProgressIndicator().pushState();
    context.getProgressIndicator().setText(CompilerBundle.message("progress.copying.resources"));

    final Collection<ProcessingItem> processed = new ArrayList<ProcessingItem>(items.length);
    final Collection<File> filesToRefresh = new THashSet<File>();

    for (ProcessingItem item : items) {
      if (item instanceof FakeProcessingItem) {
        continue;
      }

      final VirtualFile sourceVFile = item.getFile();
      final File sourceFile = new File(sourceVFile.getPath());

      context.getProgressIndicator().setText2(FlexBundle.message("copying.0", FileUtil.toSystemDependentName(sourceVFile.getPath())));

      boolean allCopied = true;

      for (String targetPath : ((FlexResourceProcessingItem)item).myTargetPaths) {
        if (context.getProgressIndicator().isCanceled()) {
          allCopied = false;
          break;
        }

        final File targetFile = new File(targetPath);
        try {
          FileUtil.copyContent(sourceFile, targetFile);
          filesToRefresh.add(targetFile);
        }
        catch (IOException e) {
          allCopied = false;
          context.addMessage(CompilerMessageCategory.ERROR,
                             CompilerBundle.message("error.copying", sourceFile.getPath(), targetFile.getPath(), e.getMessage()),
                             VfsUtil.pathToUrl(sourceFile.getPath()), -1, -1);
        }
      }

      if (allCopied) {
        processed.add(item);
      }
    }

    CompilerUtil.refreshIOFiles(filesToRefresh);

    context.getProgressIndicator().popState();

    return processed.toArray(new ProcessingItem[processed.size()]);
  }

  public ValidityState createValidityState(final DataInput in) throws IOException {
    final int size = in.readInt();
    final ArrayList<Pair<String, Long>> pathsAndTimestamps = new ArrayList<Pair<String, Long>>(size);
    for (int i = 0; i < size; i++) {
      final String path = in.readUTF();
      final long timestamp = in.readLong();
      pathsAndTimestamps.add(Pair.create(path, timestamp));
    }
    return new FlexResourceValidityState(pathsAndTimestamps);
  }

  private static class FlexResourceProcessingItem implements ProcessingItem {
    private @NotNull final VirtualFile myFile;
    private final Collection<String> myTargetPaths;

    private FlexResourceProcessingItem(final @NotNull VirtualFile sourceFile, final Collection<String> targetPaths) {
      myFile = sourceFile;
      myTargetPaths = targetPaths;
    }

    @NotNull
    public VirtualFile getFile() {
      return myFile;
    }

    public ValidityState getValidityState() {
      final Collection<Pair<String, Long>> pathsAndTimestamps = new ArrayList<Pair<String, Long>>(myTargetPaths.size());
      for (String path : myTargetPaths) {
        pathsAndTimestamps.add(Pair.create(path, new File(path).lastModified()));
      }
      return new FlexResourceValidityState(pathsAndTimestamps);
    }
  }

  private static class FlexResourceValidityState implements ValidityState {

    private final Collection<Pair<String, Long>> myPathsAndTimestamps;

    private FlexResourceValidityState(Collection<Pair<String, Long>> pathsAndTimestamps) {
      myPathsAndTimestamps = pathsAndTimestamps;
    }

    public boolean equalsTo(final ValidityState otherState) {
      return otherState instanceof FlexResourceValidityState &&
             Comparing.haveEqualElements(myPathsAndTimestamps, ((FlexResourceValidityState)otherState).myPathsAndTimestamps);
    }

    public void save(final DataOutput out) throws IOException {
      out.writeInt(myPathsAndTimestamps.size());
      for (Pair<String, Long> pathAndTimestamp : myPathsAndTimestamps) {
        out.writeUTF(pathAndTimestamp.first);
        out.writeLong(pathAndTimestamp.second);
      }
    }
  }


  private static class FakeProcessingItem implements ProcessingItem {

    private final VirtualFile myFile;

    private FakeProcessingItem(final VirtualFile file) {
      myFile = file;
    }

    @NotNull
    @Override
    public VirtualFile getFile() {
      return myFile;
    }

    @Override
    public ValidityState getValidityState() {
      return new EmptyValidityState();
    }
  }
}

