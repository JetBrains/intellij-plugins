package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This cache is used to skip module compilation if nothing has changed since last successful compilation.
 * Module is either Flex module or Java module with Flex facets.
 */
public class FlexCompilerDependenciesCache {

  private final Project myProject;
  public final Map<Module, ModuleDependencies> myModuleToDependenciesCache = new THashMap<Module, ModuleDependencies>();
  private final Set<VirtualFile> myDirtyFiles = new THashSet<VirtualFile>();

  private static final Pattern OUTPUT_FILE_IS_UP_TO_DATE =
    Pattern.compile("(\\[.*\\] )?(.+) is up-to-date and does not have to be rebuilt.");

  private static final String OUTPUT_FILE_TAG = "<flex-config><output>";

  private static final String[] TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE =
    {"<flex-config><compiler><external-library-path><path-element>", "<flex-config><compiler><local-font-paths><path-element>",
      "<flex-config><compiler><library-path><path-element>", "<flex-config><compiler><namespaces><namespace><manifest>",
      "<flex-config><compiler><source-path><path-element>", "<flex-config><compiler><theme><filename>", "<flex-config><include-file><path>",
      "<flex-config><include-sources><path-element>", "<flex-config><include-stylesheet><path>", "<flex-config><file-specs><path-element>",
      "<flex-config><compiler><include-libraries><library>"
      // "<flex-config><output>"   intentionally excluded, because already handled
    };

  public FlexCompilerDependenciesCache(final Project project) {
    myProject = project;
  }

  public void clear() {
    myModuleToDependenciesCache.clear();
    myDirtyFiles.clear();
  }

  public boolean isNothingChangedSincePreviousCompilation(final Module module) {
    clearDirtyModules();
    return myModuleToDependenciesCache.containsKey(module);
  }

  public void markModuleAndDependentModulesDirty(final Module module) {
    if (myModuleToDependenciesCache.remove(module) != null) {
      clearForDependentModules(module);
    }
  }

  public void addDirtyFile(final VirtualFile file) {
    myDirtyFiles.add(file);
  }

  public void cacheModuleWithDependencies(final CompileContext context,
                                          final Module module,
                                          final Collection<List<VirtualFile>> allConfigFiles) {
    myModuleToDependenciesCache.remove(module);
    for (List<VirtualFile> configFiles : allConfigFiles) {
      cacheModuleWithDependencies(context, module, configFiles, true);
    }
  }

  public void cacheModuleWithDependencies(final CompileContext context, final Module module, final List<VirtualFile> configFiles) {
    myModuleToDependenciesCache.remove(module);
    cacheModuleWithDependencies(context, module, configFiles, false);
  }

  private void cacheModuleWithDependencies(final CompileContext context,
                                           final Module module,
                                           final List<VirtualFile> configFiles,
                                           final boolean append) {
    clearDirtyModules();

    final VirtualFile outputFile = getOutputFile(context.getMessages(CompilerMessageCategory.INFORMATION), configFiles);
    if (outputFile != null) {
      final HashSet<VirtualFile> fileDependencies = new HashSet<VirtualFile>();
      fileDependencies.add(outputFile);
      final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (sdk != null) {
        fileDependencies.add(sdk.getHomeDirectory());
      }

      for (VirtualFile configFile : configFiles) {
        fileDependencies.add(configFile);
        fileDependencies.addAll(getDependenciesExceptSourceRootsFromConfigFile(module, configFile));
      }

      appendCssAndRespectiveSwfFiles(fileDependencies, module);

      if (append) {
        final ModuleDependencies dependencies = myModuleToDependenciesCache.get(module);
        if (dependencies  == null) {
          myModuleToDependenciesCache.put(module, ModuleDependencies.create(module, fileDependencies));
        }
        else {
          dependencies.myFiles.addAll(fileDependencies);
        }
      }
      else {
        myModuleToDependenciesCache.put(module, ModuleDependencies.create(module, fileDependencies));
      }
    }
  }

  private static void appendCssAndRespectiveSwfFiles(final Set<VirtualFile> dependencies, final Module module) {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();

    for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      if (config.DO_BUILD) {
        for (final String cssFilePath : config.CSS_FILES_LIST) {
          final VirtualFile cssFile = localFileSystem.findFileByPath(cssFilePath);
          if (cssFile != null && !fileIndex.isInSourceContent(cssFile)) {
            dependencies.add(cssFile);
          }

          final String swfFilePath = FlexCompilationUtils.createCssConfig(config, cssFilePath).getOutputFileFullPath();
          VirtualFile swfFile = localFileSystem.findFileByPath(swfFilePath);
          if (swfFile == null) {
            swfFile = FlexCompilationManager.refreshAndFindFileInWriteAction(module.getProject(), swfFilePath);
          }

          if (swfFile != null) {
            dependencies.add(swfFile);
          }
        }
      }
    }
  }

  private void clearForDependentModules(final Module module) {
    if (!myModuleToDependenciesCache.isEmpty()) {

      final Runnable runnable = new Runnable() {
        public void run() {
          for (Module dependentModule : ModuleUtil.getAllDependentModules(module)) {
            if (myModuleToDependenciesCache.remove(dependentModule) != null) {
              clearForDependentModules(dependentModule);
            }
          }
        }
      };


      if (ApplicationManager.getApplication().isReadAccessAllowed()) {
        runnable.run();
      }
      else {
        ApplicationManager.getApplication().runReadAction(runnable);
      }
    }
  }

  @Nullable
  private VirtualFile getOutputFile(final CompilerMessage[] messages, final List<VirtualFile> configFiles) {
    try {
      final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
      final VirtualFile configFile = configFiles.get(configFiles.size() - 1);
      final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), OUTPUT_FILE_TAG);
      if (outputFilePath == null) return null;
      final VirtualFile outputFile =
        FlexCompilationManager.refreshAndFindFileInWriteAction(myProject, outputFilePath, configFile.getParent().getPath());
      if (outputFile == null) return null;

      for (final CompilerMessage message : messages) {
        final String text = message.getMessage();
        final Matcher matcher1 = FlexCompilationManager.OUTPUT_FILE_CREATED_PATTERN.matcher(text);
        if (matcher1.matches()) {
          final String outputFilePath1 = matcher1.group(2);
          final int size;
          size = Integer.parseInt(matcher1.group(3));
          if (outputFile.equals(localFileSystem.findFileByPath(outputFilePath1)) && size == outputFile.getLength()) {
            return outputFile;
          }
        }
        else {
          final Matcher matcher2 = OUTPUT_FILE_IS_UP_TO_DATE.matcher(text);
          if (matcher2.matches()) {
            final String outputFilePath2 = matcher2.group(2);
            if (outputFile.equals(localFileSystem.findFileByPath(outputFilePath2))) {
              return outputFile;
            }
          }
        }
      }
    }
    catch (NumberFormatException e) {/*ignore*/}
    catch (IOException e) {/*ignore*/}
    return null;
  }

  private static Collection<VirtualFile> getDependenciesExceptSourceRootsFromConfigFile(final Module module, final VirtualFile configFile) {
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

    try {
      final Map<String, List<String>> elementsMap =
        FlexUtils.findXMLElements(configFile.getInputStream(), Arrays.asList(TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE));
      for (List<String> filePathList : elementsMap.values()) {
        for (String filePath : filePathList) {
          if (filePath.endsWith(FlexCompilerHandler.LOCALE_TOKEN)) {
            filePath = filePath.substring(0, filePath.length() - FlexCompilerHandler.LOCALE_TOKEN.length());
          }

          VirtualFile file = localFileSystem.findFileByPath(filePath);
          if (file == null) {
            final VirtualFile dir = FlexUtils.getFlexCompilerWorkDir(module.getProject(), null);
            file = VfsUtil.findRelativeFile(filePath, dir);
            if (file == null && dir != configFile.getParent()) {
              file = VfsUtil.findRelativeFile(filePath, configFile.getParent());
            }
          }

          if (file != null) {
            boolean inSourceRoot = false;
            for (VirtualFile sourceRoot : sourceRoots) {
              if (VfsUtil.isAncestor(sourceRoot, file, false)) {
                inSourceRoot = true;
                break;
              }
            }

            if (!inSourceRoot) {
              result.add(file);
            }
          }
        }
      }
    }
    catch (IOException e) {/*ignore*/}
    return result;
  }

  private void clearDirtyModules() {
    clearModulesWithChangedRoots();
    clearModulesWithDirtyFiles();
    myDirtyFiles.clear();
  }

  private void clearModulesWithChangedRoots() {
    final Collection<Module> modulesToClear = new LinkedList<Module>();

    for (final Map.Entry<Module, ModuleDependencies> entry : myModuleToDependenciesCache.entrySet()) {
      final Module module = entry.getKey();
      final ModuleDependencies dependencies = entry.getValue();

      final String[] sourceRootUrls = ModuleRootManager.getInstance(module).getSourceRootUrls();
      final Collection<String> orderEntryUrls = ModuleDependencies.getOrderEntryUrls(ModuleRootManager.getInstance(module));

      if (!Comparing.equal(dependencies.mySourceRootUrls, sourceRootUrls) ||
          !Comparing.haveEqualElements(dependencies.myOrderEntryUrls, orderEntryUrls)) {
        modulesToClear.add(module);
      }
    }

    for (Module module : modulesToClear) {
      markModuleAndDependentModulesDirty(module);
    }
  }

  private void clearModulesWithDirtyFiles() {
    for (final VirtualFile file : myDirtyFiles) {
      if (myModuleToDependenciesCache.isEmpty()) {
        break;
      }

      clearModulesIfSourceRoot(file);
      clearModulesThatDependOnFile(file);
    }
  }

  private void clearModulesIfSourceRoot(final VirtualFile file) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    final Module module = fileIndex.getModuleForFile(file);
    if (module != null) {
      final VirtualFile sourceRoot = fileIndex.getSourceRootForFile(file);
      if (sourceRoot != null) {
        markModuleAndDependentModulesDirty(module);
      }
    }
  }

  private void clearModulesThatDependOnFile(final VirtualFile dirtyFile) {
    final Collection<Module> modulesToClear = new ArrayList<Module>();

    for (final Map.Entry<Module, ModuleDependencies> entry : myModuleToDependenciesCache.entrySet()) {
      final Module module = entry.getKey();
      final ModuleDependencies dependencies = entry.getValue();

      for (final VirtualFile file : dependencies.myFiles) {
        if (file.equals(dirtyFile) ||
            (file.isDirectory() && VfsUtil.isAncestor(file, dirtyFile, false)) ||
            (dirtyFile.isDirectory() && VfsUtil.isAncestor(dirtyFile, file, false))) {
          modulesToClear.add(module);
          break;
        }
      }
    }

    for (Module module : modulesToClear) {
      markModuleAndDependentModulesDirty(module);
    }
  }

  private static class ModuleDependencies {
    private final Set<VirtualFile> myFiles;
    private final String[] mySourceRootUrls;
    private final Collection<String> myOrderEntryUrls;

    private ModuleDependencies(final Set<VirtualFile> files, final String[] sourceRootUrls, final Collection<String> orderEntryUrls) {
      myFiles = files;
      mySourceRootUrls = sourceRootUrls;
      myOrderEntryUrls = orderEntryUrls;
    }

    public static ModuleDependencies create(final Module module, final HashSet<VirtualFile> fileDependencies) {
      final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
      return new ModuleDependencies(fileDependencies, rootManager.getSourceRootUrls(), getOrderEntryUrls(rootManager));
    }

    private static Collection<String> getOrderEntryUrls(final ModuleRootManager rootManager) {
      final Collection<String> result = new LinkedList<String>();

      for (final OrderEntry orderEntry : rootManager.getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry) {
          for (final String _url : ((LibraryOrderEntry)orderEntry).getRootUrls(OrderRootType.CLASSES)) {
            final String url = _url.toLowerCase();
            if (url.endsWith(".swc") || url.endsWith(".swc!/")) {
              result.add(_url);
            }
          }
        }
        else if (orderEntry instanceof ModuleOrderEntry) {
          result.add(((ModuleOrderEntry)orderEntry).getModuleName());
        }
      }

      return result;
    }
  }
}
