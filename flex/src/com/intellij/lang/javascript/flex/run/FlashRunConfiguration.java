package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EmptyRunProfileState;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexRefactoringListenerProvider;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.PackageIndex;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

public class FlashRunConfiguration extends LocatableConfigurationBase
  implements RunProfileWithCompileBeforeLaunchOption, RefactoringListenerProvider {

  private FlashRunnerParameters myRunnerParameters = new FlashRunnerParameters();

  public FlashRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @Override
  public FlashRunConfiguration clone() {
    final FlashRunConfiguration clone = (FlashRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlashRunConfigurationForm(getProject());
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = new FlashRunnerParameters();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final FlexBuildConfiguration config;
    try {
      config = myRunnerParameters.checkAndGetModuleAndBC(getProject()).second;
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    final BuildConfigurationNature nature = config.getNature();
    if (nature.isDesktopPlatform() ||
        (nature.isMobilePlatform() && myRunnerParameters.getMobileRunTarget() == AirMobileRunTarget.Emulator)) {
      return new AirRunState(getProject(), env, myRunnerParameters);
    }

    return EmptyRunProfileState.INSTANCE;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    myRunnerParameters.check(getProject());
    myRunnerParameters.reportWarnings(getProject());
  }

  @NotNull
  public FlashRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @Override
  public Module @NotNull [] getModules() {
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());
    if (module != null && ModuleType.get(module) instanceof FlexModuleType) {
      return new Module[]{module};
    }
    else {
      return Module.EMPTY_ARRAY;
    }
  }

  @Override
  public String suggestedName() {
    return myRunnerParameters.suggestName();
  }

  @Override
  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    if (!myRunnerParameters.isOverrideMainClass()) {
      return null;
    }

    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());

    if (!(element instanceof PsiDirectoryContainer) && !(element instanceof JSPackage) && !(element instanceof JSPackageStatement)
        && (module == null || !module.equals(ModuleUtilCore.findModuleForPsiElement(element)))) {
      return null;
    }

    final String currentPackage = StringUtil.getPackageName(myRunnerParameters.getOverriddenMainClass());
    if ((element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement) &&
        Comparing.strEqual(FlexRefactoringListenerProvider.getPackageName(element), currentPackage)) {
      return new FlexRunConfigRefactoringListener.PackageRefactoringListener(this);
    }

    if (element instanceof PsiDirectory && containsClass(module, ((PsiDirectory)element), myRunnerParameters.getOverriddenMainClass())) {
      return new FlexRunConfigRefactoringListener.PsiDirectoryRefactoringListener(this);
    }

    final JSClass jsClass = FlexRefactoringListenerProvider.getJSClass(element);
    if (jsClass != null && Comparing.strEqual(jsClass.getQualifiedName(), myRunnerParameters.getOverriddenMainClass())) {
      return new FlexRunConfigRefactoringListener.JSClassRefactoringListener(this);
    }

    return null;
  }

  public static boolean containsClass(final Module module, final PsiDirectory directory, final String className) {
    final String packageName = PackageIndex.getInstance(module.getProject()).getPackageNameByDirectory(directory.getVirtualFile());
    if (!StringUtil.getPackageName(className).equals(packageName)) return false;

    final PsiElement psiElement = ActionScriptClassResolver.findClassByQNameStatic(className, GlobalSearchScope.moduleScope(module));
    return psiElement instanceof JSClass && directory.equals(psiElement.getContainingFile().getParent());
  }

  public static class AirRunState extends CommandLineState {

    private final Project myProject;
    private final BCBasedRunnerParameters myRunnerParameters;

    public AirRunState(final Project project, ExecutionEnvironment env, final BCBasedRunnerParameters runnerParameters) {
      super(env);
      myProject = project;
      myRunnerParameters = runnerParameters;
    }

    @Override
    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
      final FlexBuildConfiguration bc;
      try {
        bc = myRunnerParameters.checkAndGetModuleAndBC(myProject).second;
      }
      catch (RuntimeConfigurationError e) {
        throw new ExecutionException(e.getMessage());
      }

      final Sdk sdk = bc.getSdk();
      assert sdk != null;

      final boolean needToRemoveAirRuntimeDir;
      final VirtualFile airRuntimeDirForFlexmojosSdk;

      if (sdk.getSdkType() instanceof FlexmojosSdkType) {
        final Pair<VirtualFile, Boolean> airRuntimeDirInfo;
        try {
          airRuntimeDirInfo = FlexSdkUtils.getAirRuntimeDirInfoForFlexmojosSdk(sdk);
        }
        catch (IOException e) {
          throw new ExecutionException(e.getMessage());
        }
        needToRemoveAirRuntimeDir = airRuntimeDirInfo.second;
        airRuntimeDirForFlexmojosSdk = airRuntimeDirInfo.first;
      }
      else {
        needToRemoveAirRuntimeDir = false;
        airRuntimeDirForFlexmojosSdk = null;
      }

      final String airRuntimePath = airRuntimeDirForFlexmojosSdk == null ? null : airRuntimeDirForFlexmojosSdk.getPath();
      final OSProcessHandler processHandler =
        JavaCommandLineStateUtil.startProcess(FlexBaseRunner.createAdlCommandLine(myProject, myRunnerParameters, bc, airRuntimePath));

      if (needToRemoveAirRuntimeDir && airRuntimeDirForFlexmojosSdk != null) {
        processHandler.addProcessListener(new ProcessAdapter() {
          @Override
          public void processTerminated(@NotNull final ProcessEvent event) {
            FlexUtils.removeFileLater(airRuntimeDirForFlexmojosSdk);
          }
        });
      }

      return processHandler;
    }
  }
}
