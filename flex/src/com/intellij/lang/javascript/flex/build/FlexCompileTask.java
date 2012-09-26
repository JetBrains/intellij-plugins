package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.FileProcessingCompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.pom.Navigatable;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class FlexCompileTask implements CompileTask {

  public boolean execute(final CompileContext context) {
    if (!CompilerWorkspaceConfiguration.getInstance(context.getProject()).useOutOfProcessBuild()) {
      return true;
    }

    if (validateConfiguration(context)) {
      final FlexCompiler flexCompiler = FlexCompiler.getInstance(context.getProject());
      final FileProcessingCompiler.ProcessingItem[] items = flexCompiler.getProcessingItems(context);
      if (items.length > 0) {
        flexCompiler.process(context, items);
      }
    }

    return true;
  }

  private static boolean validateConfiguration(final CompileContext context) {
    try {
      final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCsToCompile =
        FlexCompiler.getModulesAndBCsToCompile(context.getCompileScope());

      final Collection<Trinity<Module, FlexIdeBuildConfiguration, FlashProjectStructureProblem>> problems =
        FlexCompiler.getProblems(context.getCompileScope(), modulesAndBCsToCompile);

      if (!problems.isEmpty()) {
        reportProblems(context, problems);
        return false;
      }
    }
    catch (ConfigurationException e) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("project.setup.problem", e.getMessage()), null, -1, -1);
      return false;
    }

    return true;
  }

  private static void reportProblems(final CompileContext context,
                                     final Collection<Trinity<Module, FlexIdeBuildConfiguration, FlashProjectStructureProblem>> problems) {
    // todo remove this senseless error message when 'show first error in editor' functionality respect canNavigateToSource()
    context.addMessage(CompilerMessageCategory.ERROR, "Flash build configurations contain errors. See details below.", null, -1, -1);

    for (Trinity<Module, FlexIdeBuildConfiguration, FlashProjectStructureProblem> trinity : problems) {
      final Module module = trinity.getFirst();
      final FlexIdeBuildConfiguration bc = trinity.getSecond();
      final FlashProjectStructureProblem problem = trinity.getThird();

      final String message = FlexBundle.message("bc.0.module.1.problem.2", bc.getName(), module.getName(), problem.errorMessage);
      context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1, new BCProblemNavigatable(module, bc.getName(), problem));
    }
  }

  private static class BCProblemNavigatable implements Navigatable {
    @NotNull private final Module myModule;
    @NotNull private final String myBCNme;
    @NotNull private final FlashProjectStructureProblem myProblem;

    private BCProblemNavigatable(final @NotNull Module module,
                                 final @NotNull String bcName,
                                 final @NotNull FlashProjectStructureProblem problem) {
      myModule = module;
      myBCNme = bcName;
      myProblem = problem;
    }

    public boolean canNavigateToSource() {
      return false;
    }

    public boolean canNavigate() {
      return !myModule.isDisposed() && FlexBuildConfigurationManager.getInstance(myModule).findConfigurationByName(myBCNme) != null;
    }

    public void navigate(final boolean requestFocus) {
      final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(myModule.getProject());

      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), configurable, new Runnable() {
        public void run() {
          final Place place = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, myBCNme);
          place.putPath(CompositeConfigurable.TAB_NAME, myProblem.tabName);
          place.putPath(FlexIdeBCConfigurable.LOCATION_ON_TAB, myProblem.locationOnTab);

          configurable.navigateTo(place, true);
        }
      });
    }
  }
}
