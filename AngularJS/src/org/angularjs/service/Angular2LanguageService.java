package org.angularjs.service;


import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerService;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCompletionsRequestArgs;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetErrCommand;
import com.intellij.lang.typescript.compiler.ui.TypeScriptServerServiceSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.service.protocol.Angular2LanguageServiceProtocol;
import org.angularjs.service.protocol.command.Angular2CompletionsCommand;
import org.angularjs.service.protocol.command.Angular2GetHtmlErrorCommand;
import org.angularjs.service.protocol.command.Angular2GetProjectHtmlErrCommand;
import org.angularjs.settings.AngularSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider.checkServiceIsAvailable;

public class Angular2LanguageService extends TypeScriptServerServiceImpl {

  private static final ParameterizedCachedValueProvider<VirtualFile, Project> CACHE_SERVICE_PATH_PROVIDER =
    new ParameterizedCachedValueProvider<VirtualFile, Project>() {


      @Nullable
      @Override
      public CachedValueProvider.Result<VirtualFile> compute(Project project) {
        VirtualFile result = findServiceDirectoryImpl(project);

        return CachedValueProvider.Result.create(result, JSProjectUtil.FILE_SYSTEM_STRUCTURE_MODIFICATION_TRACKER);
      }
    };

  public static final Key<ParameterizedCachedValue<VirtualFile, Project>> NG_SERVICE_PATH_KEY = Key.create("CACHED_NG_SERVICE_PATH");
  private final Condition<VirtualFile> myFileFilter;

  public Angular2LanguageService(@NotNull Project project,
                                 @NotNull TypeScriptCompilerSettings settings) {
    super(project, settings, "Angular Console");

    myFileFilter = Conditions
      .or(super.getAcceptableFilesFilter(), (el) -> el != null && el.isInLocalFileSystem() && el.getFileType() == HtmlFileType.INSTANCE);
  }

  @Nullable
  @Override
  protected JSLanguageServiceProtocol createProtocol(Consumer<?> readyConsumer) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    VirtualFile directory = getServiceDirectory(myProject);
    if (directory == null) {
      return null;
    }

    String path = directory.getCanonicalPath();
    if (path == null) {
      return null;
    }
    return new Angular2LanguageServiceProtocol(myProject, path, mySettings, readyConsumer);
  }

  public static VirtualFile getServiceDirectory(Project project) {
    return CachedValuesManager.getManager(project)
      .getParameterizedCachedValue(project, NG_SERVICE_PATH_KEY, CACHE_SERVICE_PATH_PROVIDER, false, project);
  }

  @Nullable
  private static VirtualFile findServiceDirectoryImpl(Project project) {
    for (VirtualFile file : ProjectRootManager.getInstance(project).getContentRoots()) {
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
    }

    return null;
  }

  @NotNull
  @Override
  protected TypeScriptGetErrCommand createGetErrCommand(@NotNull VirtualFile file, @NotNull String path) {
    if (file.getFileType() == HtmlFileType.INSTANCE) {
      return new Angular2GetHtmlErrorCommand(path);
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
      TransactionGuard.submitTransaction(this, () -> defaultService.terminateStartedProcess(false));

    }

    return super.createLanguageServiceQueue();
  }


  public static boolean isEnabledAngularService(Project project) {
    return AngularSettings.get(project).isUseService() &&
           AngularIndexUtil.hasAngularJS2(project) &&
           getServiceDirectory(project) != null;
  }

  @Nullable
  @Override
  public TypeScriptServerServiceSettings getServiceSettings() {
    return AngularSettings.get(myProject);
  }
}
