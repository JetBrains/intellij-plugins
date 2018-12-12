package org.angular2.service;


import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.service.JSLanguageServiceCacheableCommand;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.lang.javascript.service.protocol.*;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerService;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCompletionsRequestArgs;
import com.intellij.lang.typescript.compiler.ui.TypeScriptServerServiceSettings;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.angular2.service.protocol.Angular2LanguageServiceProtocol;
import org.angular2.service.protocol.command.Angular2CompletionsCommand;
import org.angular2.service.protocol.command.Angular2GetHtmlErrCommand;
import org.angular2.service.protocol.command.Angular2GetProjectHtmlErrCommand;
import org.angular2.settings.AngularSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings.TypeScriptCompilerVersionType.EMBEDDED;
import static com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider.checkServiceIsAvailable;

public class Angular2LanguageService extends TypeScriptServerServiceImpl {

  private static final ParameterizedCachedValueProvider<Collection<VirtualFile>, Project> CACHE_SERVICE_PATH_PROVIDER =
    project -> {
      Collection<VirtualFile> result = findServiceDirectoriesImpl(project);
      return CachedValueProvider.Result.create(result, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
    };

  public static final Key<ParameterizedCachedValue<Collection<VirtualFile>, Project>> NG_SERVICE_PATH_KEY =
    Key.create("CACHED_NG_SERVICE_PATH");
  private final Condition<VirtualFile> myFileFilter;

  public Angular2LanguageService(@NotNull Project project,

                                 @NotNull TypeScriptCompilerSettings settings) {
    super(project, settings, "Angular Console");

    myFileFilter = Conditions
      .or(super.getAcceptableFilesFilter(), (el) -> {
        if (el != null && el.isInLocalFileSystem() && el.getFileType() == HtmlFileType.INSTANCE) {
          VirtualFile config = TypeScriptConfigUtil.getNearestParentConfig(el);
          return config != null;
        }

        return false;
      });
  }

  @Nullable
  @Override
  protected JSLanguageServiceProtocol createProtocol(Consumer<?> readyConsumer, @NotNull String tsServicePath) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    VirtualFile directory = getServiceDirectory(myProject);
    if (directory == null) {
      return null;
    }

    String path = directory.getCanonicalPath();
    if (path == null) {
      return null;
    }
    return new Angular2LanguageServiceProtocol(myProject, path, mySettings, readyConsumer, createEventConsumer(), tsServicePath);
  }

  public static VirtualFile getServiceDirectory(@NotNull Project project) {
    Collection<VirtualFile> result = CachedValuesManager.getManager(project)
      .getParameterizedCachedValue(project, NG_SERVICE_PATH_KEY, CACHE_SERVICE_PATH_PROVIDER, false, project);

    //todo anstarovoyt multi-projects support
    return result.size() == 1 ? ContainerUtil.getFirstItem(result) : null;
  }

  @NotNull
  private static Collection<VirtualFile> findServiceDirectoriesImpl(@NotNull Project project) {
    if (project.isDefault() || project.isDisposed()) return ContainerUtil.emptyList();

    ApplicationManager.getApplication().assertReadAccessAllowed();

    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null) return ContainerUtil.emptyList();
    VirtualFile rootService = searchInRootDirectory(baseDir);
    if (rootService != null) return Collections.singleton(rootService);

    Collection<TypeScriptConfig> configFiles = TypeScriptConfigService.Provider.getConfigFiles(project);
    if (configFiles.isEmpty()) return ContainerUtil.emptyList();

    return configFiles.stream()
      .map(el -> el.getConfigDirectory())
      .map(dir -> searchInRootDirectory(dir)).filter(el -> el != null)
      .collect(Collectors.toList());
  }

  @Nullable
  private static VirtualFile searchInRootDirectory(@NotNull VirtualFile file) {
    if (file.isInLocalFileSystem() && file.isDirectory()) {
      VirtualFile modules = file.findChild("node_modules");
      if (modules != null) {
        VirtualFile angularPackage = modules.findChild("@angular");
        if (angularPackage != null) {
          VirtualFile serviceDirectory = angularPackage.findChild("language-service");
          if (serviceDirectory != null) {
            return serviceDirectory;
          }
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  protected Collection<JSLanguageServiceCacheableCommand> createGetErrCommand(@NotNull VirtualFile file, @NotNull String path) {
    if (file.getFileType() == Angular2HtmlFileType.INSTANCE) {
      String configFile = getConfigForFile(file);
      if (configFile == null) return ContainerUtil.emptyList();
      Angular2GetHtmlErrCommand error = new Angular2GetHtmlErrCommand(path);
      error.arguments.projectFileName = LocalFilePath.create(configFile);
      return Collections.singletonList(error);
    }
    return super.createGetErrCommand(file, path);
  }

  @Override
  public boolean canHighlight(@NotNull PsiFile file) {
    if (file instanceof HtmlFileImpl) {
      return checkServiceIsAvailable(myProject, this, mySettings);
    }

    return super.canHighlight(file);
  }

  @NotNull
  @Override
  protected String getProcessName() {
    return "Angular";
  }

  @NotNull
  @Override
  public Condition<VirtualFile> getAcceptableFilesFilter() {
    return myFileFilter;
  }

  @Override
  @NotNull
  protected JSLanguageServiceSimpleCommand createCompletionCommand(@NotNull TypeScriptCompletionsRequestArgs args,
                                                                   @NotNull VirtualFile virtualFile,
                                                                   @NotNull PsiFile file) {
    return file instanceof XmlFile || virtualFile.getFileType() instanceof XmlLikeFileType ?
           new Angular2CompletionsCommand(args) :
           super.createCompletionCommand(args, virtualFile, file);
  }

  @NotNull
  @Override
  protected JSLanguageServiceCommand createProjectCommand(@NotNull VirtualFile file, @NotNull String path) {
    FileType type = file.getFileType();
    return type instanceof XmlLikeFileType ? new Angular2GetProjectHtmlErrCommand(path) : super.createProjectCommand(file, path);
  }

  @Nullable
  @Override
  protected JSLanguageServiceQueue createLanguageServiceQueue() {
    TypeScriptCompilerService defaultService = TypeScriptCompilerService.getDefaultService(myProject);
    if (defaultService.isServiceCreated()) {
      JSLanguageServiceQueue.LOGGER.info("Dispose default service by " + getProcessName());
      //dispose old service
      TransactionGuard.submitTransaction(this, () -> defaultService.terminateStartedProcess(false, true));
    }

    return super.createLanguageServiceQueue();
  }

  @Nullable
  @Override
  protected String getConfigForFile(@NotNull VirtualFile file) {
    if (file.getFileType() instanceof XmlLikeFileType) {
      return ReadAction.compute(() -> {
        if (myProject.isDisposed()) return null;
        VirtualFile tsFile = findSiblingTsFile(file);
        //we can have .spec.json and .app.json on the same level so let's use linked ts-file config if possible
        if (tsFile != null) return super.getConfigForFile(file);

        Collection<TypeScriptConfig> allConfigs = TypeScriptConfigService.Provider.getConfigFiles(myProject);
        TypeScriptConfig config = TypeScriptConfigUtil.getNearestParentConfig(file, allConfigs);

        return config == null ? null : TypeScriptCompilerConfigUtil.normalizeNameAndPath(config.getConfigFile());
      });
    }

    return super.getConfigForFile(file);
  }

  @Nullable
  private static VirtualFile findSiblingTsFile(@NotNull VirtualFile file) {
    VirtualFile parent = file.getParent();
    String nameWithoutExtension = file.getNameWithoutExtension();
    return parent.findChild(nameWithoutExtension + TypeScriptUtil.TYPESCRIPT_FILE_EXTENSION);
  }

  public static boolean isEnabledAngularService(@NotNull PsiElement element) {
    return isEnabledAngularService(element.getProject()) &&
           Angular2LangUtil.isAngular2Context(element);
  }

  public static boolean isEnabledAngularService(Project project, @NotNull VirtualFile file) {
    return isEnabledAngularService(project) &&
           Angular2LangUtil.isAngular2Context(project, file);
  }

  private static boolean isEnabledAngularService(Project project) {
    return AngularSettings.get(project).isUseService() &&
           TypeScriptCompilerSettings.getSettings(project).getVersionType() != EMBEDDED &&
           getServiceDirectory(project) != null;
  }

  @Nullable
  @Override
  public TypeScriptServerServiceSettings getServiceSettings() {
    return AngularSettings.get(myProject);
  }

  @NotNull
  @Override
  protected List<JSAnnotationError> parseGetErrorResult(@NotNull JSLanguageServiceAnswer answer, String path) {
    return ContainerUtil.filter(super.parseGetErrorResult(answer, path), error -> !error.getDescription()
      .startsWith("ng: Parser Error:"));
  }
}
