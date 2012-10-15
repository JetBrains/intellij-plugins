package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexRefactoringListenerProvider;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.FlexRunConfigRefactoringListener;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class FlexUnitRunConfiguration extends RunConfigurationBase
  implements RunProfileWithCompileBeforeLaunchOption, LocatableConfiguration, RefactoringListenerProvider {

  private FlexUnitRunnerParameters myRunnerParameters = new FlexUnitRunnerParameters();

  protected FlexUnitRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlexUnitRunConfigurationForm(getProject());
  }

  public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider provider) {
    return null;
  }

  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final ProgramRunner runner) {
    return null;
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = new FlexUnitRunnerParameters();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    XmlSerializer.serializeInto(myRunnerParameters, element);
  }

  public FlexUnitRunConfiguration clone() {
    final FlexUnitRunConfiguration clone = (FlexUnitRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }

  @NotNull
  public FlexUnitRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  public String suggestedName() {
    switch (myRunnerParameters.getScope()) {
      case Class:
        return StringUtil.getShortName(myRunnerParameters.getClassName());
      case Method:
        return StringUtil.getShortName(myRunnerParameters.getClassName()) + "." + myRunnerParameters.getMethodName() + "()";
      case Package:
        return StringUtil.isEmpty(myRunnerParameters.getPackageName())
               ? myRunnerParameters.getModuleName()
               : myRunnerParameters.getPackageName();
      default:
        assert false : "Unknown scope: " + myRunnerParameters.getScope();
        return null;
    }
  }

  public boolean isGeneratedName() {
    return getName().startsWith(ExecutionBundle.message("run.configuration.unnamed.name.prefix")) ||
           Comparing.equal(getName(), suggestedName());
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

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    final FlexIdeBuildConfiguration bc;
    try {
      bc = myRunnerParameters.checkAndGetModuleAndBC(getProject()).second;
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e.getMessage());
    }

    final BuildConfigurationNature nature = bc.getNature();
    if (nature.isDesktopPlatform() || nature.isMobilePlatform()) {

      final FlashRunConfiguration.AirRunState airRunState =
        new FlashRunConfiguration.AirRunState(getProject(), env, myRunnerParameters) {
          @Override
          public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
            final ProcessHandler processHandler = startProcess();
            final ExecutionConsole console = FlexBaseRunner.createFlexUnitRunnerConsole(getProject(), env, processHandler, executor);
            return new DefaultExecutionResult(console, processHandler);
          }
        };
      airRunState.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
      return airRunState;
    }

    return FlexBaseRunner.EMPTY_RUN_STATE;
  }

  public void checkConfiguration() throws RuntimeConfigurationError {
    myRunnerParameters.check(getProject());
  }

  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    final FlexUnitRunnerParameters params = getRunnerParameters();
    final Module module = ModuleManager.getInstance(getProject()).findModuleByName(params.getModuleName());
    if (!(element instanceof PsiDirectoryContainer) && !(element instanceof JSPackage) && !(element instanceof JSPackageStatement)
        && (module == null || !module.equals(ModuleUtil.findModuleForPsiElement(element)))) {
      return null;
    }

    switch (params.getScope()) {
      case Method:
        if (element instanceof JSFunction) {
          final PsiElement parent = element.getParent();
          if (parent instanceof JSClass &&
              Comparing.strEqual(((JSClass)parent).getQualifiedName(), params.getClassName()) &&
              Comparing.strEqual(((JSFunction)element).getName(), params.getMethodName())) {
            return new FlexRunConfigRefactoringListener.JSFunctionRefactoringListener(this);
          }
        }
        // no break here!
      case Class:
        if (element instanceof PsiDirectory &&
            FlashRunConfiguration.containsClass(module, ((PsiDirectory)element), params.getClassName())) {
          return new FlexRunConfigRefactoringListener.PsiDirectoryRefactoringListener(this);
        }

        final JSClass jsClass = FlexRefactoringListenerProvider.getJSClass(element);
        if (jsClass != null && Comparing.strEqual(jsClass.getQualifiedName(), params.getClassName())) {
          return new FlexRunConfigRefactoringListener.JSClassRefactoringListener(this);
        }
        // no break here!
      case Package:
        final String currentPackage = params.getScope() == FlexUnitRunnerParameters.Scope.Package
                                      ? params.getPackageName()
                                      : StringUtil.getPackageName(params.getClassName());
        if ((element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement) &&
            Comparing.strEqual(FlexRefactoringListenerProvider.getPackageName(element), currentPackage)) {
          return new FlexRunConfigRefactoringListener.PackageRefactoringListener(this);
        }
    }

    return null;
  }
}
