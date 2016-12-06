package org.angularjs.service;


import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import com.intellij.util.Consumer;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.service.protocol.Angular2LanguageServiceProtocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  public Angular2LanguageService(@NotNull Project project,
                                 @NotNull TypeScriptCompilerSettings settings) {
    super(project, settings);
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

  @Override
  public boolean isAcceptable(@NotNull VirtualFile file) {
    return super.isAcceptable(file) && isEnabledAngularService(myProject);
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

  public static boolean isEnabledAngularService(Project project) {
    return AngularIndexUtil.hasAngularJS2(project) && getServiceDirectory(project) != null;
  }
}
