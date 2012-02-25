package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;

public class FlashRunConfiguration extends RunConfigurationBase
  implements RunProfileWithCompileBeforeLaunchOption, LocatableConfiguration {

  private FlashRunnerParameters myRunnerParameters = new FlashRunnerParameters();

  public FlashRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  public FlashRunConfiguration clone() {
    final FlashRunConfiguration clone = (FlashRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlashRunConfigurationForm(getProject());
  }

  public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider provider) {
    return null;
  }

  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final ProgramRunner runner) {
    return null;
  }

  @Override
  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = new FlashRunnerParameters();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final FlexIdeBuildConfiguration config;
    try {
      config = myRunnerParameters.checkAndGetModuleAndBC(getProject()).second;
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    final BuildConfigurationNature nature = config.getNature();
    if (nature.isDesktopPlatform() ||
        (nature.isMobilePlatform() && myRunnerParameters.getMobileRunTarget() == AirMobileRunTarget.Emulator)) {
      final AirRunState airRunState = new AirRunState(getProject(), env, myRunnerParameters);
      airRunState.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
      return airRunState;
    }

    return FlexBaseRunner.EMPTY_RUN_STATE;
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    myRunnerParameters.check(getProject());
  }

  @NotNull
  public FlashRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  public Module[] getModules() {
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());
    if (module != null && ModuleType.get(module) instanceof FlexModuleType) {
      return new Module[]{module};
    }
    else {
      return Module.EMPTY_ARRAY;
    }
  }

  public boolean isGeneratedName() {
    return getName().startsWith(ExecutionBundle.message("run.configuration.unnamed.name.prefix")) ||
           Comparing.equal(getName(), suggestedName());
  }

  public String suggestedName() {
    return myRunnerParameters.suggestName();
  }

  /*
  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    //assert !(this instanceof FlexUnitRunConfiguration); // FlexUnitRunConfiguration must have own implementation

    if ((this instanceof AirRunConfiguration &&
         ((AirRunnerParameters)myRunnerParameters).getAirRunMode() != AirRunnerParameters.AirRunMode.MainClass)
        ||
        (!(this instanceof AirRunConfiguration) && myRunnerParameters.getRunMode() != FlexRunnerParameters.RunMode.MainClass)) {
      return null;
    }

    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(myRunnerParameters.getModuleName());

    if (!(element instanceof PsiDirectoryContainer) && !(element instanceof JSPackage) && !(element instanceof JSPackageStatement)
        && (module == null || !module.equals(ModuleUtil.findModuleForPsiElement(element)))) {
      return null;
    }

    String currentPackage = StringUtil.getPackageName(myRunnerParameters.getMainClassName());
    if ((element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement) &&
        Comparing.strEqual(FlexRefactoringListenerProvider.getPackageName(element), currentPackage)) {
      //return new FlexRunConfigRefactoringListener.PackageRefactoringListener(this);
    }

    if (element instanceof PsiDirectory && containsClass(module, ((PsiDirectory)element), myRunnerParameters.getMainClassName())) {
      //return new FlexRunConfigRefactoringListener.PsiDirectoryRefactoringListener(this);
    }

    final JSClass jsClass = FlexRefactoringListenerProvider.getJSClass(element);
    if (jsClass != null && Comparing.strEqual(jsClass.getQualifiedName(), myRunnerParameters.getMainClassName())) {
      //return new FlexRunConfigRefactoringListener.JSClassRefactoringListener(this);
    }

    return null;
  }

  private static boolean containsClass(final Module module, final PsiDirectory directory, final String className) {
    final String packageName = DirectoryIndex.getInstance(module.getProject()).getPackageName(directory.getVirtualFile());
    if (!StringUtil.getPackageName(className).equals(packageName)) return false;

    final PsiElement psiElement = JSResolveUtil.findClassByQName(className, GlobalSearchScope.moduleScope(module));
    return psiElement instanceof JSClass && directory.equals(psiElement.getContainingFile().getParent());
  }
  */

  public static class AirRunState extends CommandLineState {

    private final Project myProject;
    private final BCBasedRunnerParameters myRunnerParameters;

    public AirRunState(final Project project, ExecutionEnvironment env, final BCBasedRunnerParameters runnerParameters) {
      super(env);
      myProject = project;
      myRunnerParameters = runnerParameters;
    }

    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
      final FlexIdeBuildConfiguration bc;
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
        JavaCommandLineStateUtil.startProcess(FlexBaseRunner.createAdlCommandLine(myRunnerParameters, bc, airRuntimePath));

      if (needToRemoveAirRuntimeDir && airRuntimeDirForFlexmojosSdk != null) {
        processHandler.addProcessListener(new ProcessAdapter() {
          public void processTerminated(final ProcessEvent event) {
            FlexUtils.removeFileLater(airRuntimeDirForFlexmojosSdk);
          }
        });
      }

      return processHandler;
    }
  }
}
