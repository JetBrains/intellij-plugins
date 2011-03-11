package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexRefactoringListenerProvider;
import com.intellij.lang.javascript.flex.run.AirRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.FlexRunConfigRefactoringListener;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

public class FlexUnitRunConfiguration extends AirRunConfiguration {

  protected FlexUnitRunConfiguration(Project project, ConfigurationFactory factory, String name) {
    super(project, factory, name);
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlexUnitRunConfigurationForm(getProject());
  }

  public static Pair<Module, FlexUnitSupport> getFlexUnitSupport(Project project, String moduleName) throws RuntimeConfigurationError {
    final Module module = getAndValidateModule(project, moduleName);
    FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(module);

    if (flexUnitSupport == null) {
      throw new RuntimeConfigurationError(FlexBundle.message("flexunit.not.found", module.getName()));
    }
    return Pair.create(module, flexUnitSupport);
  }

  public void checkConfiguration() throws RuntimeConfigurationError {
    if (DumbService.getInstance(getProject()).isDumb()) return;

    final FlexUnitRunnerParameters params = getRunnerParameters();
    final Pair<Module, FlexUnitSupport> flexUnitInfo = getFlexUnitSupport(getProject(), params.getModuleName());

    final GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(flexUnitInfo.first);
    switch (params.getScope()) {
      case Class:
        getClassToTest(params.getClassName(), moduleScope, flexUnitInfo.second, true);
        break;

      case Method:
        final JSClass classToTest = getClassToTest(params.getClassName(), moduleScope, flexUnitInfo.second, false);
        if (StringUtil.isEmpty(params.getMethodName())) {
          throw new RuntimeConfigurationError(FlexBundle.message("no.test.method.specified"));
        }

        final JSFunction methodToTest = classToTest.findFunctionByNameAndKind(params.getMethodName(), JSFunction.FunctionKind.SIMPLE);

        if (methodToTest == null || !flexUnitInfo.second.isTestMethod(methodToTest)) {
          throw new RuntimeConfigurationError(FlexBundle.message("method.not.valid", params.getMethodName()));
        }
        break;

      case Package:
        if (!JSUtils.packageExists(params.getPackageName(), moduleScope)) {
          throw new RuntimeConfigurationError(FlexBundle.message("package.not.valid", params.getPackageName()));
        }
        break;

      default:
        assert false : "Unknown scope: " + params.getScope();
    }

    checkDebuggerSdk();
  }

  private static JSClass getClassToTest(String className,
                                        GlobalSearchScope searchScope,
                                        @NotNull FlexUnitSupport flexUnitSupport,
                                        boolean allowSuite) throws RuntimeConfigurationError {
    if (StringUtil.isEmpty(className)) {
      throw new RuntimeConfigurationError(FlexBundle.message("test.class.not.specified"));
    }
    final PsiElement classToTest = JSResolveUtil.findClassByQName(className, searchScope);
    if (!(classToTest instanceof JSClass)) {
      throw new RuntimeConfigurationError(FlexBundle.message("class.not.found", className));
    }

    if (!flexUnitSupport.isTestClass((JSClass)classToTest, allowSuite)) {
      throw new RuntimeConfigurationError(
        FlexBundle.message(allowSuite ? "class.not.test.class.or.suite" : "class.not.test.class", className));
    }
    return (JSClass)classToTest;
  }

  @Override
  protected FlexRunnerParameters createRunnerParametersInstance() {
    return new FlexUnitRunnerParameters();
  }

  @NotNull
  @Override
  public FlexUnitRunnerParameters getRunnerParameters() {
    return (FlexUnitRunnerParameters)super.getRunnerParameters();
  }

  public static String getLauncherTemplate(boolean flexUnit4Present) throws IOException {
    final URL resource =
      FlexUnitRunConfiguration.class.getResource(flexUnit4Present ? "FlexUnit4Launch.template" : "FlexUnit1Launch.template");
    return ResourceUtil.loadText(resource);
  }

  public static String getLauncherBaseText() throws IOException {
    final URL resource = FlexUnitRunConfiguration.class.getResource("FlexUnitLauncherBase.template");
    return ResourceUtil.loadText(resource);
  }

  public static String getLogTargetText() throws IOException {
    final URL resource = FlexUnitRunConfiguration.class.getResource("LogTarget.template");
    return ResourceUtil.loadText(resource);
  }

  @Override
  public String suggestedName() {
    final FlexUnitRunnerParameters params = getRunnerParameters();
    switch (params.getScope()) {
      case Class:
        return StringUtil.getShortName(params.getClassName());
      case Method:
        return StringUtil.getShortName(params.getClassName()) + "." + params.getMethodName() + "()";
      case Package:
        return StringUtil.isEmpty(params.getPackageName()) ? params.getModuleName() : params.getPackageName();
      default:
        assert false : "Unknown scope: " + params.getScope();
        return null;
    }
  }

  @Override
  public FlexUnitRunConfiguration clone() {
    return (FlexUnitRunConfiguration)super.clone();
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return new AirRunState(env) {
      @Override
      public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        final ProcessHandler processHandler = startProcess();
        final ExecutionConsole console = FlexBaseRunner.createFlexUnitRunnerConsole(getProject(), env, processHandler, executor);
        return new DefaultExecutionResult(console, processHandler);
      }
    };
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
        if (element instanceof PsiDirectory && containsClass(module, ((PsiDirectory)element), params.getClassName())) {
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
